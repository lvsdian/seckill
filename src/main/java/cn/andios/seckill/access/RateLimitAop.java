package cn.andios.seckill.access;

import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @description:
 * @author:LSD
 * @when:2019/12/10/15:58
 */
@Component
@Aspect
public class RateLimitAop {

    private final static Logger logger = LoggerFactory.getLogger(RateLimitAop.class);

    private RateLimiter rateLimiter = RateLimiter.create(Double.MAX_VALUE);

    /**
     * 切入点
     */
    @Pointcut("@annotation(cn.andios.seckill.access.RateLimit)")
    public void pointCut(){};

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        logger.info("拦截到{}..."+proceedingJoinPoint.getSignature().getName());
        Signature signature = proceedingJoinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        //获取目标方法
        Method targetMethod = methodSignature.getMethod();
        if(targetMethod.isAnnotationPresent(RateLimit.class)) {
            RateLimit rateLimit = targetMethod.getAnnotation(RateLimit.class);
            rateLimiter.setRate(rateLimit.limit());
            //如果没有获取到令牌
            if (!rateLimiter.tryAcquire(rateLimit.timeOut(), rateLimit.timeOutUnit())) {
                logger.info(proceedingJoinPoint.getSignature().getName()+"没有获取到令牌，返回...");
                return Result.error(CodeMsg.RATE_LIMIT);
            }
        }
        logger.info(proceedingJoinPoint.getSignature().getName()+"获取到令牌，通过...");
        return proceedingJoinPoint.proceed();
    }
}
