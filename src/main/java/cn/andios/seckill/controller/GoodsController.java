package cn.andios.seckill.controller;

import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.redis.GoodsKey;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.redis.SecKillUserKey;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.GoodsService;
import cn.andios.seckill.service.SecKillUserService;
import cn.andios.seckill.vo.GoodsDetailVo;
import cn.andios.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/18:49
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    private SecKillUserService secKillUserService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 压测用，无其他意义
     * @param model
     * @param secKillUser
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public String info(Model model,SecKillUser secKillUser){
        model.addAttribute("user",secKillUser);
        return "goods_list";
    }


    /**
     *  登录成功后，访问to_list接口进入商品列表页，
     *  接收token等参数，判断用户是否已经登录，返回字符串，由Thymeleaf解析成页面
     * @param model
     * @param response
     * @param cookieToken
     * @param paramCookie  如果是手机端，可能把cookie放在了参数里
     * @return
     */
    @RequestMapping("/to_list1")
    public String list1(Model model,HttpServletResponse response,
                          @CookieValue (value = SecKillUserService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
                          @RequestParam(value = SecKillUserService.COOKIE_NAME_TOKEN,required = false) String paramCookie){
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramCookie)){
            return "login";
        }
        String token = StringUtils.isEmpty(paramCookie)?cookieToken:paramCookie;
        SecKillUser secKillUser = secKillUserService.getSecKillUserByToken(response,token);
        model.addAttribute("user",secKillUser);
        return "goods_list";
    }


    /**
     * 其他方法也需要token等参数来判断用户是否登录，所以将token等参数在UserArgumentResolver中做了统一处理，直接接收SecKillUser参数即可
     * 依旧是返回字符串，由Thymeleaf解析成页面
     *
     * 压测：
     *  QPS:1267
     *  并发数：5000 * 10
     *  load:15
     * @param model
     * @param secKillUser
     * @return
     */
    @RequestMapping("/to_list2")
    public String list2(Model model,SecKillUser secKillUser){
        if(secKillUser == null){
            return "login";
        }
        model.addAttribute("user",secKillUser);
        List<GoodsVo> goodsVoList = goodsService.listSecKillGoodsVo();
        //商品列表
        model.addAttribute("goodsList",goodsVoList);
        return "goods_list";
    }

    /**
     * 在list2的基础上，添加了页面缓存：
     *  1. 先从redis中获取秒杀商品列表页面，
     *      1.1 如果获取到了(那么本次访问不是第一次)，直接将缓存的页面返回
     *      1.2 如果没有获取到，就查数据库得到秒杀商品列表，将secKillUser和secKillGoodsList放入model中，
     *          再将model,request,response等等参数通过Thymeleaf生成页面，把页面放入redis。下次访问就会直接从redis中取
     *
     * 压测：
     *  QPS:2884
     *  并发数：5000 * 10
     *  load:5
     *
     * @param model
     * @param secKillUser
     * @return
     */
    @RequestMapping(value = "/to_list3",produces = "text/html")
    @ResponseBody
    public String list3(Model model, SecKillUser secKillUser, HttpServletRequest request, HttpServletResponse response){
        if(secKillUser == null){
            return "login";
        }
        //从缓存中取
        String html = redisService.get(GoodsKey.getSecKillGoodsList, "", String.class);
        //如果取到了，返回
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //如果没取到
        model.addAttribute("user",secKillUser);
        List<GoodsVo> goodsVoList = goodsService.listSecKillGoodsVo();
        //商品列表
        model.addAttribute("goodsList",goodsVoList);
        //return "goods_list";

        //手动渲染
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);

        //如果不为空，存入redis
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getSecKillGoodsList,"",html);
        }
        return html;
    }

    /**
     * 用户在goods_list.html页面点击商品详情，访问/goods/to_detail/{goodsId}接口
     * 根据前台传过来的goodsId，去数据库中查找goods,然后放到model中。返回字符串goods_detail，由Thymeleaf解析成页面goods_detail.html
     * 没有用到redis，也没有做前端页面静态化
     * @param model
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping("/to_detail1/{goodsId}")
    public String detail1(Model model, SecKillUser secKillUser, @PathVariable("goodsId")Long goodsId){
        model.addAttribute("user",secKillUser);
        //商品列表
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goodsVo);

        int secKillStatus = 0;
        int remainSecond = 0;

        Long start = goodsVo.getStartDate().getTime();
        Long end = goodsVo.getEndDate().getTime();
        Long now = System.currentTimeMillis();
        if(now < start){
            //秒杀还未开始
            secKillStatus = 0;
            remainSecond = (int) ((start - now)/1000);
        }else if(now > end){
            //秒杀已经结束
            secKillStatus = 2;
            remainSecond = -1;
        }else{
            secKillStatus = 1;
            remainSecond = 0;
        }
        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("remainSecond",remainSecond);

        return "goods_detail";
    }

    /**
     * 使用redis缓存页面做优化，得到商品id后，先根据id从redis缓存中取商品详情页页面，如果取到了，直接返回
     * 如果没取到，就将商品、用户等参数通过Thymeleaf生成goods_detail.html页面，返回此详情页。如果页面不为空，就存入redis
     * @param model
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail2(Model model, SecKillUser secKillUser, @PathVariable("goodsId")Long goodsId,
                         HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("user",secKillUser);

        //从缓存中取
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        //如果取到了，返回
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //取不到，从数据库中查
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goodsVo);

        int secKillStatus = 0;
        int remainSecond = 0;

        Long start = goodsVo.getStartDate().getTime();
        Long end = goodsVo.getEndDate().getTime();
        Long now = System.currentTimeMillis();
        if(now < start){
            //秒杀还未开始
            secKillStatus = 0;
            remainSecond = (int) ((start - now)/1000);
        }else if(now > end){
            //秒杀已经结束
            secKillStatus = 2;
            remainSecond = -1;
        }else{
            secKillStatus = 1;
            remainSecond = 0;
        }
        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("remainSecond",remainSecond);

//        return "goods_detail";

        //手动渲染
        WebContext webContext = new WebContext(request,response, request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        //如果不为空，存入redis
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    /**
     * 页面静态化处理，使用前后端分离，前面两个方法都返回页面，这里返回的是Result,携带GoodsDetailVo对象，这个对象中包含
     * 前端页面所需要的数据，前端通过JQuery将数据对象填充到页面，所示在goods_list.html页面查看商品详情有两种，
     *  一种是访问前面两个接口，他们最终都会返回页面
     *  也可以直接访问页面goods_detail.htm，在这个页面中会请求/to_detail3/{goodsId}接口，根据id拿到数据，再渲染页面
     * @param secKillUser
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/to_detail3/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail3(SecKillUser secKillUser, @PathVariable("goodsId")Long goodsId){
        //商品列表
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        int secKillStatus = 0;
        int remainSecond = 0;

        Long start = goodsVo.getStartDate().getTime();
        Long end = goodsVo.getEndDate().getTime();
        Long now = System.currentTimeMillis();
        if(now < start){
            //秒杀还未开始
            secKillStatus = 0;
            remainSecond = (int) ((start - now)/1000);
        }else if(now > end){
            //秒杀已经结束
            secKillStatus = 2;
            remainSecond = -1;
        }else{
            secKillStatus = 1;
            remainSecond = 0;
        }
        //返回GoodsDetailVo
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setSecKillUser(secKillUser);
        goodsDetailVo.setRemainSecond(remainSecond);
        goodsDetailVo.setSecKillStatus(secKillStatus);
        return Result.success(goodsDetailVo);
    }
}
