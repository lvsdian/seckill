package cn.andios.seckill.domain;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:16
 */
@Data
public class Order {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long deliveryAddrId;
    private String goodsName;
    private Integer goodsCount;
    private Double goodsPrice;
    private Integer orderChannel;
    private Integer status;
    private Date createDate;
    private Date payDate;
}
