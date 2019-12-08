package cn.andios.seckill.rabbitmq;

import cn.andios.seckill.domain.SecKillUser;
import lombok.Data;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/21/23:05
 */
@Data
public class SecKillMessage {
    private SecKillUser secKillUser;
    private Long goodsVoId;

}
