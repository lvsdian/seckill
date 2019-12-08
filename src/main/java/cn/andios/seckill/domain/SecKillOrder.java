package cn.andios.seckill.domain;

import lombok.Data;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:19
 */
@Data
public class SecKillOrder {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long orderId;
}
