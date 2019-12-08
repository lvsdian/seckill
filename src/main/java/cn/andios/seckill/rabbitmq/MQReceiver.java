package cn.andios.seckill.rabbitmq;

import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillOrder;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.GoodsService;
import cn.andios.seckill.service.OrderService;
import cn.andios.seckill.service.SecKillService;
import cn.andios.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/20/18:06
 */
@Service
public class MQReceiver {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SecKillService secKillService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQConfig.DIRECT_QUEUE)
    public void receiveDirect(String message){
        logger.info("receive direct message：" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic_1(String message){
        logger.info("receive topic_1 message：" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic_2(String message){
        logger.info("receive topic_2 message：" + message);
    }

    @RabbitListener(queues = MQConfig.FANOUT_QUEUE)
    public void receiveFanout_1(String message){
        logger.info("receive fanout_1 message：" + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeaders(byte [] message){
        logger.info("receive headers_2 message：" + new String(message));
    }

    @RabbitListener(queues = MQConfig.SEC_KILL_QUEUE)
    public void receiveSecKillMessage(String message){
        logger.info("receive  message：" + message);
        //接收消息对象
        SecKillMessage secKillMessage = RedisService.stringToBean(message, SecKillMessage.class);
        SecKillUser secKillUser = secKillMessage.getSecKillUser();
        Long goodsVoId = secKillMessage.getGoodsVoId();
        //判断库存
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsVoId);
        if(goodsVo.getStockCount() <0){
            return ;
        }
        //判断是否已经秒杀到了
        SecKillOrder secKillOrder = orderService.getSecKillOrderByUserIdGoodsId(secKillUser.getId(),goodsVoId);
        if(secKillOrder != null){
            return;
        }

        //减库存、下订单、写入秒杀订单
        secKillService.secKill(secKillUser,goodsVo);
    }


}
