package cn.andios.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * @description:
 * @author:LSD
 * @when:2019/10/14/21:26
 */
@Configuration
public class RedisPoolFactory {

    @Autowired
    private RedisConfig redisConfig;

    @Bean
    public JedisPool jedisPoolFactory(){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        jedisPoolConfig.setMaxTotal(redisConfig.getPoolMaxActive());

        return new JedisPool(jedisPoolConfig,redisConfig.getHost(),redisConfig.getPort(),
                2000,redisConfig.getPassword(), redisConfig.getDatabase());
    }
}
