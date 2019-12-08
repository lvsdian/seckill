package cn.andios.seckill.dao;

import cn.andios.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:21
 */
@Mapper
@Repository
public interface GoodsDao {

    /**
     * seckill_goods左连接goods,查找所有秒杀商品信息
     *
     * @return
     */
    @Select("select g.*,sk.stock_count,sk.start_date,sk.end_date,sk.seckill_price from seckill_goods sk left join goods g on sk.goods_id = g.id")
    List<GoodsVo> listSecKillGoodsVo();

    /**
     * seckill_goods左连接goods,根据id查找商品
     * @param goodsId
     * @return
     */
    @Select("select g.*,sk.stock_count,sk.start_date,sk.end_date,sk.seckill_price from seckill_goods sk left join goods g on sk.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 根据id减少seckill_goods的商品库存
     * stock_count > 0防止两个线程同时进行，导致stock_count < 0
     * @param goodsId
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count - 1 where id=#{goodsId} and stock_count > 0")
    int reduceStock(@Param("goodsId")Long goodsId);

}
