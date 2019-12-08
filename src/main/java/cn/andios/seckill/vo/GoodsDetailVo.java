package cn.andios.seckill.vo;

import cn.andios.seckill.domain.SecKillUser;
import lombok.Data;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/19/18:25
 */
@Data
public class GoodsDetailVo {
    private int secKillStatus;
    private int remainSecond;
    private GoodsVo goodsVo;
    private SecKillUser secKillUser;
}
