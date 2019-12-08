package cn.andios.seckill.service;

import cn.andios.seckill.controller.SecKillController;
import cn.andios.seckill.dao.OrderDao;
import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillOrder;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.redis.SecKillOrderKey;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/15:56
 */
@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private RedisService redisService;

    private static Logger logger = LoggerFactory.getLogger(SecKillController.class);

    public SecKillOrder getSecKillOrderByUserIdGoodsId(Long userId, Long goodsId) {
        //下面的createOrder中，如果创建订单成功，会把订单放到redis，没有设置过期时间，所以这里不需要从数据库中查
        return redisService.get(SecKillOrderKey.getSecKillOrderByUserIdGoodsId,""+userId + "_"+goodsId,SecKillOrder.class);
    }

    @Transactional
    public Order createOrder(SecKillUser secKillUser, GoodsVo goodsVo) {
        Order order = new Order();
        order.setCreateDate(new Date());
        order.setDeliveryAddrId(0L);
        order.setGoodsCount(1);
        order.setGoodsId(goodsVo.getId());
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsPrice(goodsVo.getSecKillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setUserId(secKillUser.getId());

        orderDao.insertOrder(order);


        SecKillOrder secKillOrder = new SecKillOrder();
        secKillOrder.setGoodsId(goodsVo.getId());
        //mybatis插入后，会把id放到order中 ？？？
        secKillOrder.setOrderId(order.getId());
        secKillOrder.setUserId(secKillUser.getId());;
        orderDao.insertSecKillOrder(secKillOrder);

        logger.info("生成订单："+secKillOrder.toString());

        //下单成功，前缀+用户id+商品id为key,秒杀订单为value存入redis
        redisService.set(SecKillOrderKey.getSecKillOrderByUserIdGoodsId,""+ secKillUser.getId() + "_" +goodsVo.getId(),secKillOrder);
        return order;
    }

    public Order getOrderByOrderId(Long orderId) {
        return orderDao.getOrderByOrderId(orderId);
    }
}
