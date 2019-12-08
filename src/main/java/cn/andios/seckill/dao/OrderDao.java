package cn.andios.seckill.dao;

import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillOrder;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;


/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/16:08
 */
@Mapper
@Repository
public interface OrderDao {

    /**
     * 根据用户id和商品id查询秒杀订单
     * @param userId
     * @param goodsId
     * @return
     */
    @Select("select * from seckill_order where user_id = #{userId} and goods_id = #{goodsId}")
    SecKillOrder getSecKillOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 插入订单，返回id
     * @param order
     * @return
     */
    @Insert("insert into `seckilldb`.`order`(`user_id`,`goods_id`,`delivery_addr_id`,`goods_name`,`goods_count`,`goods_price`,`order_channel`,`status`," +
            "`create_date`)VALUES(#{userId},#{goodsId},#{deliveryAddrId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    long insertOrder(Order order);

    /**
     * 插入秒杀订单
     * @param secKillOrder
     */
    @Insert("insert into `seckilldb`.`seckill_order` (user_id,goods_id,order_id)values(#{userId},#{goodsId},#{orderId})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    void insertSecKillOrder(SecKillOrder secKillOrder);

    /**
     * 根据id获取订单
     * @param orderId
     * @return
     */
    @Select("select * from `order` where id= #{orderId}")
    Order getOrderByOrderId(Long orderId);
}
