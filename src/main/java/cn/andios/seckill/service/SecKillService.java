package cn.andios.seckill.service;

import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillOrder;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.redis.GoodsKey;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.redis.SecKillKey;
import cn.andios.seckill.util.MD5Util;
import cn.andios.seckill.util.UUIDUtil;
import cn.andios.seckill.util.VerifyCodeUtil;
import cn.andios.seckill.vo.GoodsVo;
import cn.andios.seckill.vo.VerifyCodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/16:04
 */
@Service
public class SecKillService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Transactional
    public Order secKill(SecKillUser secKillUser, GoodsVo goodsVo) {
        //减库存、下订单、写入秒杀订单
        boolean success = goodsService.reduceStock(goodsVo);
        if(success){
            return orderService.createOrder(secKillUser,goodsVo);
        }else{
            setGoodsSecKillOver(goodsVo.getId());
            return null;
        }

    }

    /**
     * 根据用户id、商品id在redis中查询订单
     * 查到了返回订单id
     * 如果没查到：
     *      如果秒杀结束，返回 -1
     *      如果秒杀没结束，还没下单，就返回 0
     * @param secKillUserId
     * @param goodsId
     * @return
     */
    public long getSecKillResult(Long secKillUserId, Long goodsId) {
        SecKillOrder secKillOrder = orderService.getSecKillOrderByUserIdGoodsId(secKillUserId, goodsId);
        //成功
        if(secKillOrder != null){
            return  secKillOrder.getOrderId();
        }else{
            boolean isSecKillOver = getGoodsSecKillOver(goodsId);
            if(isSecKillOver){
                return  -1;
            }else{
                return 0;
            }
        }
    }

    private boolean getGoodsSecKillOver(Long goodsId) {
        return redisService.exists(GoodsKey.isGoodsSecKillOver,""+goodsId);
    }

    private void setGoodsSecKillOver(Long goodsId) {
        redisService.set(GoodsKey.isGoodsSecKillOver,"" + goodsId,true);
    }

    /**
     * 一个是用户请求的path，一个是redis中的path,比对
     * @param secKillUserId
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkPath(Long secKillUserId, Long goodsId, String path) {
        if(secKillUserId == null || goodsId == null || path == null){
            return false;
        }
        String redisPath = redisService.get(SecKillKey.getSecKillPath, "" + secKillUserId + "_" + goodsId, String.class);
        return path.equals(redisPath);
    }

    public String createSecKillPath(SecKillUser secKillUser,Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid());
        //秒杀地址存入redis
        redisService.set(SecKillKey.getSecKillPath,""+secKillUser.getId()+"_"+goodsId,str);
        return str;
    }

    public BufferedImage createVerifyCode(Long secKillUserId, Long goodsId) {
        if(secKillUserId == null || goodsId == null ){
            return null;
        }
        VerifyCodeVo verifyCodeVo = VerifyCodeUtil.createVerifyCode();

        //把验证码放入redis
        Integer result = verifyCodeVo.getResult();
        redisService.set(SecKillKey.getSecKillVerifyCode,secKillUserId+"_"+goodsId,result);
        //返回bufferImage到Controller
        return verifyCodeVo.getBufferedImage();

    }

    public boolean checkVerifyCode(Long secKillUserId, Long goodsId, Integer verifyCode) {
        if(secKillUserId == null || goodsId == null || verifyCode == null){
            return false;
        }
        Integer redisVerifyCode = redisService.get(SecKillKey.getSecKillVerifyCode, secKillUserId + "_" + goodsId, Integer.class);
        if(redisVerifyCode == null || redisVerifyCode - verifyCode != 0){
            return false;
        }
        redisService.del(SecKillKey.getSecKillVerifyCode,secKillUserId + "_" + goodsId);
        return true;
    }
}
