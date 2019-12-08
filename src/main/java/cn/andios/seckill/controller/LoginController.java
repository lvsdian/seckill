package cn.andios.seckill.controller;

import cn.andios.seckill.exception.GlobeException;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.SecKillUserService;
import cn.andios.seckill.util.ValidatorUtil;
import cn.andios.seckill.vo.LoginVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;


/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/15:34
 */
@RequestMapping("/login")
@Controller
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private SecKillUserService secKillUserService;

    /**
     * 访问login/to_login跳转到login.html
     * @return
     */
    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    /**
     *  输入表单数据点击登录，login.html中的doLogin方法会对密码进行第一次md5加密，然后把账号密码传到这里。
     *  在使用JSR303校验之前：
     *      需要判断用户输入的账号密码是否为空等等，
     *      如果为空或格式不合法，就抛出以上；
     *      如果不为空，且格式合法，就在secKillUserService中与数据库中的账号密码比对校验。
     *  使用JSR303校验之后：
     *      LoginVo的属性加上注解(这里自定义实现了一个注解@IsMobile)，方法参数中加@Valid注解，表示对这个参数进行JSR303校验。result
     *      对象封装了校验的结果，如果校验发现账号密码不合要求，就抛出异常，否则就在secKillUserService中与数据库中的账号密码比对校验。
     *  如果密码账号都正确：
     *      1. 会生成uuid为token,将前缀+token为key，用户为value存入redis
     *      2. 把token放到cookie中，返回给response对象，即response存在用户的唯一标识token
     *      3. 前台访问/goods/to_list接口跳到商品列表页
     *  如果账号密码有误：
     *      1. 原来是返回Result对象，前台根据Result中的CodeMsg判断是登陆成功还是失败，成功就跳转，失败就提示错误，不跳转
     *      2. 使用全局异常处理后，如果某个数据不对，直接抛出异常，然后在异常处理时在返回Result对象，
     *          前台根据Result中的CodeMsg判断是登陆成功还是失败，成功就跳转，失败就提示错误，不跳转
     *
     * @param response
     * @param loginVo
     * @return
     */
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo, BindingResult result){
        if (result.hasErrors()) {
            //校验发现数据存在问题
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors) {
                System.out.println("错误字段名：" + error.getField());
                System.out.println("错误信息：" + error.getDefaultMessage());
            }
            throw new GlobeException(CodeMsg.MOBILE_OR_PASSWORD_ERROR);
        }
        if(loginVo == null){
            //return Result.error(CodeMsg.SESSION_ERROR);
            throw new GlobeException(CodeMsg.SESSION_ERROR);
        }
        logger.info(loginVo.toString());
        //参数校验---使用JSR303之后，这里就不需要校验，如果参数错误，会抛出异常，抛出的异常由全局异常处理器捕获
//        String passInput = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if(StringUtils.isEmpty(passInput)){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if(StringUtils.isEmpty(mobile)){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if(!ValidatorUtil.isM  obile(mobile)){
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
        //登录
        secKillUserService.login(response,loginVo);
        return Result.success(true);
    }
}
