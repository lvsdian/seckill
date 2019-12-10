package cn.andios.seckill.access;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 访问限流
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 每秒令牌数量,默认不限流
     * @return
     */
    double limit() default Double.MAX_VALUE;

    /**
     * 获取令牌的等待时间
     * @return
     */
    int timeOut() default 0;

    /**
     * 超时时间单位
     * @return
     */
    TimeUnit timeOutUnit()  default TimeUnit.SECONDS;

}
