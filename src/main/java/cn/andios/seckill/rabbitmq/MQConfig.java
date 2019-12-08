package cn.andios.seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @description:
 * @author:LSD
 * @when:2019/10/20/18:06
 */
@Configuration
public class MQConfig {

    /**
     * direct模式
     */
    public static  final String DIRECT_QUEUE = "direct_queue";

    /**
     * topic模式
     */
    public static  final String TOPIC_QUEUE1 = "topic_queue1";
    public static  final String TOPIC_QUEUE2 = "topic_queue2";
    public static  final String TOPIC_EXCHANGE = "topic_exchange";
    public static  final String ROUTING_KEY1 = "topic_routing.key1";
    /** 表示一个单词 #表示0或多个单词 */
    public static  final String ROUTING_KEY2 = "topic_routing.#";

    /**
     * fanout模式
     */
    public static  final String FANOUT_EXCHANGE = "fanout_exchange";
    public static  final String FANOUT_QUEUE = "fanout_queue1";

    /**
     * headers模式
     */
    public static final String HEADERS_EXCHANGE = "headers_exchange";
    public static final String HEADERS_QUEUE = "headers_queue";

    /**
     * 秒杀Queue
     */
    public static final String SEC_KILL_QUEUE = "sec_kill_queue";


    /**
     * direct模式 交换机Exchange
     * @return
     */
    @Bean
    public Queue queue(){
        return new Queue(DIRECT_QUEUE,true);
    }

    /**
     * topic模式 交换机Exchange
     * @return
     */
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2,true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    @Bean
    public Binding topicBing1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTING_KEY1);
    }

    @Bean
    public Binding topicBing2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTING_KEY2);
    }


    /**
     * fanout模式 交换机Exchange
     * @return
     */
    @Bean
    public Queue fanoutQueue1(){
        return new Queue(FANOUT_QUEUE,true);
    }
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }
    @Bean
    public Binding fanoutBind(){
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    /**
     * headers 模式 交换机Exchange
     * @return
     */
    public Queue headersQueue(){
        return new Queue(HEADERS_QUEUE,true);
    }

    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    @Bean
    public Binding headersBind(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("headers1","value1");
        map.put("headers2","value2");
        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
    }
}
