package cn.andios.seckill.rabbitmq;

import cn.andios.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * @description:
 * @author:LSD
 * @when:2019/10/20/18:06
 */
@Service
public class MQSender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static Logger logger = LoggerFactory.getLogger(MQSender.class);

    public void sendDirect(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send direct message：" + msg);
        amqpTemplate.convertAndSend(MQConfig.DIRECT_QUEUE,message+"_direct_msg1");
    }

    public void sendTopic(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send topic message：" + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,message + "_topic_msg1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,message + "_topic_msg2");
    }

    public void sendFanout(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send fanout message：" + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,message + "_fanout_msg1");
    }

    public void sendHeaders(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send headers message：" + msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("headers1","values1");
        messageProperties.setHeader("headers2","values2");
        Message obj = new Message(msg.getBytes(),messageProperties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
    }

    public void sendSecKillMessage(SecKillMessage secKillMessage) {
        String msg = RedisService.beanToString(secKillMessage);
        logger.info("send message：" + msg);
        amqpTemplate.convertAndSend(MQConfig.SEC_KILL_QUEUE,msg);
    }
}
