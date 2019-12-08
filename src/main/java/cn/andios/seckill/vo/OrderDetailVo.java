package cn.andios.seckill.vo;

import cn.andios.seckill.domain.Order;
import lombok.Data;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/19/20:04
 */
@Data
public class OrderDetailVo {
    private GoodsVo goodsVo;
    private Order order;
}
