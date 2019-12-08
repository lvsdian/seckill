package cn.andios.seckill.domain;

import lombok.Data;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:11
 */
@Data
public class Goods {
    private Long id;
    private String goodsName;
    private String goodsTitle;
    private String goodsImg;
    private String goodsDetail;
    private Double goodsPrice;
    private Integer goodsStock;
}
