package cn.andios.seckill.service;

import cn.andios.seckill.controller.LoginController;
import cn.andios.seckill.dao.SecKillUserDao;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.exception.GlobeException;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.redis.SecKillUserKey;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.util.MD5Util;
import cn.andios.seckill.util.UUIDUtil;
import cn.andios.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.CoderMalfunctionError;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/16:19
 */
@Service
public class SecKillUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private SecKillUserDao secKillUserDao;

    @Autowired
    private RedisService redisService;

    public SecKillUser getSecKillUserById(Long id){
        return secKillUserDao.getById(id);
    }

    /**
     *
     * @param loginVo
     */
    public void login(HttpServletResponse response,LoginVo loginVo) {
        String passInput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        SecKillUser secKillUser = getSecKillUserById(Long.parseLong(mobile));
        if(secKillUser == null){
            //return CodeMsg.MOBILE_NOT_EXIST;
            throw new GlobeException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String passDB = secKillUser.getPassword();
        String saltDB = secKillUser.getSalt();
        String passCalc = MD5Util.formPassToDBPass(passInput,saltDB);
        if(!passDB.equals(passCalc)){
            //return CodeMsg.PASSWORD_ERROR;
            throw new GlobeException(CodeMsg.PASSWORD_ERROR);
        }
        //return CodeMsg.SUCCESS;

        //生成cookie
        //带上用户标识存入redis
        String token = UUIDUtil.uuid();
        addCookie(response,secKillUser,token);
    }

    private void addCookie(HttpServletResponse response,SecKillUser secKillUser,String token) {
        redisService.set(SecKillUserKey.token,token,secKillUser);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        //创建cookie，设置cookie过期时间与键过期时间一致
        cookie.setMaxAge(SecKillUserKey.token.getExpireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public SecKillUser getSecKillUserByToken(HttpServletResponse response,String token) {

        if(StringUtils.isEmpty(token)){
            return null;
        }
        SecKillUser secKillUser = redisService.get(SecKillUserKey.token, token, SecKillUser.class);
        //有效期需要延长
        if(secKillUser != null){
            addCookie(response,secKillUser,token);
        }
        return secKillUser;
    }

    /**
     * 对象缓存，这里只是demo,没有实际用到
     * @param id
     * @return
     */
    public SecKillUser getById(Long id){
        //取缓存
        SecKillUser secKillUser = redisService.get(SecKillUserKey.getById, "" + id, SecKillUser.class);
        if(secKillUser != null){
            return secKillUser;
        }
        //如果缓存中没有，从数据库中取
        secKillUser = secKillUserDao.getById(id);
        //放入缓存
        if(secKillUser != null){
            redisService.set(SecKillUserKey.getById,"" + id,secKillUser);
        }

        return secKillUser;
    }

    /**
     * 对象缓存 ，更新对象时注意要更新缓存(先更新数据库，再更新缓存)(只是demo,没有实际用到)
     * @param token
     * @param id
     * @param newPassword
     * @return
     */
    public  boolean updatePassword(String token,Long id,String newPassword){
        //取user
        SecKillUser secKillUser = getSecKillUserById(id);
        if(secKillUser == null){
            throw new GlobeException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        SecKillUser newSecKillUser = new SecKillUser();
        newSecKillUser.setId(id);
        newSecKillUser.setPassword(MD5Util.inputPassToFormPass(newPassword,secKillUser.getSalt()));
        secKillUserDao.updatePassword(newSecKillUser);
        //更新缓存
        redisService.del(SecKillUserKey.getById,"" + id);
        secKillUser.setPassword(newSecKillUser.getPassword());
        redisService.set(SecKillUserKey.token,token,secKillUser);
        return true;
    }
}
