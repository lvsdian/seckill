package cn.andios.seckill.controller;

import cn.andios.seckill.access.AccessLimit;
import cn.andios.seckill.access.RateLimit;
import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillOrder;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.rabbitmq.MQSender;
import cn.andios.seckill.rabbitmq.SecKillMessage;
import cn.andios.seckill.redis.AccessKey;
import cn.andios.seckill.redis.GoodsKey;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.redis.SecKillKey;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.GoodsService;
import cn.andios.seckill.service.OrderService;
import cn.andios.seckill.service.SecKillService;
import cn.andios.seckill.util.MD5Util;
import cn.andios.seckill.util.UUIDUtil;
import cn.andios.seckill.vo.GoodsVo;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/15:48
 */
@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SecKillService secKillService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    /**
     * 创建一个map作为内存标记，key为秒杀商品id，value表示是否秒杀结束，减少redis访问
     */
    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    private static final Logger logger = LoggerFactory.getLogger(SecKillController.class);

    /**
     * 用户在goods_detail.html中点击立即秒杀,会携带商品id访问do_secKill1这个接口执行秒杀
     *
     * 根据商品id进行秒杀，大致逻辑：根据商品id找到商品对象，先判断库存是否大于0；再根据用户信息查询当前用户是否已经参与秒杀，
     * 如果已经有秒杀订单生成，直接返回，否则三连(减库存、下订单、写入秒杀订单)
     *
     * 如果不做优化，这个方法会访问4次数据库
     * 如下，do_secKill2为页面静态化优化；do_secKill3为接口优化；do_secKill4为安全优化
     *
     * @param model
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping("/do_secKill1")
    public String secKill1(Model model, SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId) {
        if (secKillUser == null) {
            return "login";
        }
        model.addAttribute("user", secKillUser);

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        //判断库存
        int stock = goodsVo.getStockCount();
        if (stock < 0) {
            model.addAttribute("errMsg", CodeMsg.SEC_KILL_OVER.getMsg());
            return "secKill_fail";
        }
        //不能重复秒杀
        SecKillOrder secKillOrder = orderService.getSecKillOrderByUserIdGoodsId(secKillUser.getId(), goodsId);
        if (secKillOrder != null) {
            model.addAttribute("errMsg", CodeMsg.REPEAT_SEC_KILL.getMsg());
            return "secKill_fail";
        }
        //减库存、下订单、写入秒杀订单
        Order order = secKillService.secKill(secKillUser, goodsVo);

        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "order_detail";
    }


    /**
     * 页面静态化，前后端分离--优化
     *
     *  用户在goods_detail.htm中点击立即秒杀,会携带商品id访问do_secKill3这个接口执行秒杀
     *  前后端分离改进后，原来返回的是字符串，由Thymeleaf解析成页面，这里返回Result，里面放的数据对象order,前端拿到order后再渲染页面
     *
     * get:幂等,它代表从服务端获取数据，无论调用多少次，产生的结果一样，不会对服务端数据产生任何影响
     * post:向服务端提交数据，对服务端数据产生了变化，就用post
     *
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_secKill2", method = RequestMethod.POST)
    @ResponseBody
    public Result<Order> secKill2(SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId) {
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        /**
         * 如果同一个用户同时发来了两个请求(虽说前台也只了验证码，不让用户同时发两个请求，但还要考虑这样情况)，
         * 判断他的订单时，订单为空，这是下单，就会下两次单，违背了只能秒杀一次的规定
         * 解决方法：seckill_order表加一个唯一索引，栏位为 user_id.goods_id
         */
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        //判断库存
        int stock = goodsVo.getStockCount();
        if (stock < 0) {
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //不能重复秒杀
        SecKillOrder secKillOrder = orderService.getSecKillOrderByUserIdGoodsId(secKillUser.getId(), goodsId);
        if (secKillOrder != null) {
            return Result.error(CodeMsg.REPEAT_SEC_KILL);
        }
        //减库存、下订单、写入秒杀订单
        Order order = secKillService.secKill(secKillUser, goodsVo);

        return Result.success(order);
    }

    /**
     * 接口优化--优化
     *
     *  将本类实现InitializingBean接口，重写它的afterPropertiesSet方法，在系统初始化时就会执行这个方法。
     *  在afterPropertiesSet中，查询数据库中的所有商品，将 前缀+商品id为key，库存为value存入redis，
     *  1. 内存标记。创建一个map，键为商品id，值为此商品是否秒杀结束。请求过来时，先用map判断这个商品是否秒杀完了，如果秒杀完了，
     *      就直接返回，如果没有，再查询redis
     *  2. redis预减库存。请求过来时，经过map判断秒杀还未结束后，就在redis中将库存减1，然后把商品id、用户信息封装成消息对象，放到rabbitmq
     *      队列中，在消息接收方(MQReceiver#receiveSecKillMessage(java.lang.String))中处理秒杀；本方法中直接返回“排队中”的状态信息。完成异步下单。
     *
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_secKill3", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> secKill3(SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId) {
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //内存标记,减少redis访问
        boolean isSecKillGoodsOver = localOverMap.get(goodsId);

        if(isSecKillGoodsOver){
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //实现InitializingBean接口，实现afterPropertiesSet方法，它会在系统初始化时执行
        //系统初始化时，afterPropertiesSet方法中会把当前秒杀商品加入redis,后面请求就不访问数据库
        //1.预减库存
        Long stock = redisService.decr(GoodsKey.getSecKillGoodsStock, "" + goodsId);
        if (stock < 0) {
            //这里设为true，如果再有请求过来，就不会访问redis了
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //2.请求入队
        SecKillMessage secKillMessage = new SecKillMessage();
        secKillMessage.setGoodsVoId(goodsId);
        secKillMessage.setSecKillUser(secKillUser);
        mqSender.sendSecKillMessage(secKillMessage);
        //排队中
        return Result.success(0);
    }

    /**
     * 路径加入path--安全优化
     *
     * 相比于do_secKill3：
     *  原来用户点击在goods_detail.htm中点击立即秒杀时，会访问do_secKill3接口直接执行秒杀
     *  现在点击立即秒杀会访问path接口，path接口中校验验证码后，会返回一个path字符串，再带着这个字符串来访问/secKill/{path}/do_secKill4进行秒杀
     *
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/do_secKill4", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> secKill4(SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId,
                                    @PathVariable("path")String path) {
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean isValid = secKillService.checkPath(secKillUser.getId(),goodsId,path);
        if(!isValid){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记,减少redis访问
        boolean isSecKillGoodsOver = localOverMap.get(goodsId);

        if(isSecKillGoodsOver){
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //实现InitializingBean接口，实现afterPropertiesSet方法，它会在系统初始化时执行
        //系统初始化时，afterPropertiesSet方法中会把当前秒杀商品加入redis,后面请求就不访问数据库
        //1.预减库存
        Integer result = redisService.get(GoodsKey.getSecKillGoodsStock,""+goodsId,Integer.class);
        System.out.println("result =============================="+result);
        Long stock = redisService.decr(GoodsKey.getSecKillGoodsStock, "" + goodsId);
        if (stock < 0) {
            //这里设为true，如果再有请求过来，就不会访问redis了
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //2.请求入队
        SecKillMessage secKillMessage = new SecKillMessage();
        secKillMessage.setGoodsVoId(goodsId);
        secKillMessage.setSecKillUser(secKillUser);
        mqSender.sendSecKillMessage(secKillMessage);
        //排队中
        return Result.success(0);
    }

    /**
     * goods_detail.htm中用户点击立即秒杀后，不会直接访问do_secKill3接口进行秒杀，而是携带验证码访问path接口，
     * 校验验证码时先从redis缓存中根据用户id及商品id取出验证码，然后比对用户输入的验证码，验证通过后，再生成随机字符串path
     * 以前缀+用户id+商品id为key,path为value存入redis中，将path返回给goods_detail.htm页面中。goods_detail.htm拿到path后再访问
     * /secKill/{path}/do_secKill4接口进行秒杀。
     *
     * AccessLimit是自定义注解，起限流防刷作用，如下，表示在5s内最多请求10次，访问这个方法需要登录，配置needLogin=true后也就不需要
     * 判断secKillUser == null
     *
     * RateLimit起限流作用，AccessLimit防止一个用户短时间内多次请求，RateLimit是限制所有用户总体的请求数量
     *
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @RateLimit(limit = 10,timeOut = 1,timeOutUnit = TimeUnit.SECONDS)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> secKillPath(SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId,
                                      @RequestParam("verifyCode")Integer verifyCode, HttpServletRequest request) {
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //查询访问次数,防刷(5s最多访问5次)，这一部分在cn.andios.seckill.access.AccessInterceptor中做了统一处理
//        String uri = request.getRequestURI();
//        String key = uri + "_" + secKillUser.getId();
//        Integer count = redisService.get(AccessKey.access, key, Integer.class);
//        if(count == null){
//            redisService.set(AccessKey.access,key,1);
//        }else if(count <5){
//            redisService.incr(AccessKey.access,key);
//        }else{
//            return Result.error(CodeMsg.ACCESS_LIMIT);
//        }

        //校验验证码
        boolean isVerifyCodeRight = secKillService.checkVerifyCode(secKillUser.getId(),goodsId,verifyCode);

        if(!isVerifyCodeRight){
            return  Result.error(CodeMsg.VERIFY_CODE_ERROR);
        }
        //根据用户id和商品id生成path
        String path = secKillService.createSecKillPath(secKillUser,goodsId);
        return Result.success(path);
    }


    /**
     * 系统初始化，查询所有商品，前缀+商品id为key,商品库存为键放入redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listSecKillGoodsVo();
        if (goodsVoList == null) {
            return;
        }

        for (GoodsVo goodsVo : goodsVoList) {
            logger.info("添加商品库存到redis中");
            redisService.set(GoodsKey.getSecKillGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            //表明当前商品没有结束
            localOverMap.put(goodsVo.getId(), false);
        }
    }

    /**
     * 用户带着path访问/secKill/{path}/do_secKill4接口进行秒杀，在do_secKill4中做了异步下单，返回的是：Result.success(0)，
     * 如果goods_detail.htm中接收到这个返回值(表明秒杀请求在排队中)，就会带着goodsId来轮询服务端，即请求这个result接口
     *
     *  secKillService中根据用户id、商品id在redis中查询订单
     *  查到了返回订单id，即秒杀成功，跳转到order_detail.htm页面
     *  如果没查到：
     *      如果秒杀结束，返回 -1，提示秒杀失败
     *      如果秒杀没结束(表明还没下单)，返回 0，继续轮询
     *
     * AccessLimit是自定义注解，起限流防刷作用，如下，表示在5s内最多请求10次，访问这个方法需要登录
     *
     * @param model
     * @param secKillUser
     * @param goodsId
     * @return 成功：orderId  失败：-1  还在排队：0
     */
    @AccessLimit(second=5,maxCount=10,needLogin=true)
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> secKillResult(Model model, SecKillUser secKillUser,
                                      @RequestParam("goodsId") Long goodsId) {
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = secKillService.getSecKillResult(secKillUser.getId(), goodsId);
        logger.info("客户端轮询");
        return Result.success(result);
    }




    /**
     * 在秒杀进行中，用户需要输入验证码才能进行秒杀，如果商品没有在秒杀，就不要验证码，在goods_detail.htm中，
     * countDown()会判断商品当前是否正在秒杀中，正在秒杀的那个case就会请求verifyCode接口，生成验证码，返回并渲染到页面
     * secKillService中生成的验证码，会以前缀+用户id+商品id为key,验证码为value存入redis,以供/path接口中验证使用
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<BufferedImage> verifyCode(HttpServletResponse response,SecKillUser secKillUser, @RequestParam("goodsId") Long goodsId,
                                            @RequestParam(value = "timestamp",required = false)String timestamp) {
        logger.info("/verifyCode-----timestamp："+ timestamp);
        if (secKillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage bufferedImage = secKillService.createVerifyCode(secKillUser.getId(),goodsId);
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage,"JPEG",outputStream);
            outputStream.flush();
            outputStream.close();
            //验证码已通过ImageIO写到前端
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.VERIFY_CODE_FAIL);
        }
    }
}
