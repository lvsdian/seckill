package cn.andios.seckill.vo;

import cn.andios.seckill.domain.Goods;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:23
 */
@Data
public class GoodsVo extends Goods {

    private Integer stockCount;
    private Double secKillPrice;
    private Date startDate;
    private Date endDate;
}
