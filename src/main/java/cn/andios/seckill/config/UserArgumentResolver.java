package cn.andios.seckill.config;

import cn.andios.seckill.access.AccessInterceptor;
import cn.andios.seckill.access.SecKillUserContext;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.service.SecKillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:需要注册，在WebConfig中实现
 * @author:LSD
 * @when:2019/10/15/20:36
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private SecKillUserService secKillUserService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == SecKillUser.class;
    }

    @Override
    public  Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

//        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse httpServletResponse = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
//
//        String paramToken = httpServletRequest.getParameter(SecKillUserService.COOKIE_NAME_TOKEN);
//        //根据httpServletRequest从Cookies中得到cookie
//        String cookieToken = AccessInterceptor.getCookieValue(httpServletRequest,SecKillUserService.COOKIE_NAME_TOKEN);
//
//        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
//            return null;
//        }
//        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//        return secKillUserService.getSecKillUserByToken(httpServletResponse,token);


        //因为拦截器(cn.andios.seckill.access.AccessInterceptor)在参数处理(cn.andios.seckill.config.UserArgumentResolver)之前执行,
        //所以这里直接在SecKillUserContext中取，拦截器中已经根据token的参数取得了secKillUser
        return SecKillUserContext.getSecKillUser();

    }


}
