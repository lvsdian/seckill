package cn.andios.seckill.access;

import cn.andios.seckill.config.UserArgumentResolver;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.redis.AccessKey;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.SecKillService;
import cn.andios.seckill.service.SecKillUserService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @description: 访问拦截器(即自定义注解@AccessLimit的实现)，需要注册，在WebConfig中实现
 * @author:LSD
 * @when:2019/10/23/12:13
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private SecKillUserService secKillUserService;

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            //根据request和response获取秒杀用户
            SecKillUser secKillUser = getSecKillUser(request, response);
            //将用户放入ThreadLocal中,cn.andios.seckill.config.UserArgumentResolver在拦截器之后生效，所以可用直接从SecKillUserContext中取
            SecKillUserContext.setSecKillUser(secKillUser);

            HandlerMethod handlerMethod = (HandlerMethod)handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            //即拦截的方法上没有这个注解，直接放过就行
            if(accessLimit == null){
                return true;
            }
            //得到注解标注的值
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            //需要登录
            if(needLogin){
                //但secKillUser为空
                 if(secKillUser == null){
                    //除了返回false,还返回一些提示信息
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+secKillUser.getId();
            }
            //读取注解中的second(不同方法配置的second可能不同)，放入redis中
            Integer count = redisService.get(AccessKey.withExpire(second), key, Integer.class);
            if(count == null){
                //第一次访问，设置值
                redisService.set(AccessKey.withExpire(second),key,1);
            }else if(count <maxCount){
                //访问次数还没有超过最大次数限制，就把访问次数加1
                redisService.incr(AccessKey.withExpire(second),key);
            }else{
                //已经达到限制访问次数，就返回
                render(response,CodeMsg.ACCESS_LIMIT);
                return false;
            }

        }
        return true;
    }

    /**
     * 将错误信息返回给页面
     * @param response
     * @param codeMsg
     * @throws Exception
     */
    private void render(HttpServletResponse response,CodeMsg codeMsg) throws Exception{
        response.setContentType("application/json;charset=utf-8");
        OutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 根据request和response得到SecKillUser
     * @param request
     * @param response
     * @return
     */
    private SecKillUser getSecKillUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter(SecKillUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,SecKillUserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;

        return secKillUserService.getSecKillUserByToken(response,token);
    }

    public static  String getCookieValue(HttpServletRequest httpServletRequest,String cookieName) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie:cookies){
            if(cookie == null){
                return null;
            }
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
