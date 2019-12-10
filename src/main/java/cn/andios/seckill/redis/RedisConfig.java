package cn.andios.seckill.redis;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/14/19:44
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Component
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.database}")
    private Integer database;
    @Value("${spring.redis.jedis.pool.max-active}")
    private Integer poolMaxActive;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private Integer poolMaxIdle;
    @Value("${spring.redis.jedis.pool.min-idle}")
    private Integer poolMinIdle;
    @Value("${spring.redis.timeout}")
    private String timeout;
}
