package cn.andios.seckill.domain;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:15
 */
@Data
public class SecKillGoods {
    private Long id;
    private Long goodsId;
    private Double secKillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
