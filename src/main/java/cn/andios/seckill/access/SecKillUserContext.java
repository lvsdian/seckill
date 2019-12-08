package cn.andios.seckill.access;

import cn.andios.seckill.domain.SecKillUser;

/**
 * @description:ThreadLocal是绑定到当前线程的，多线程情况下不会有冲突，所以用它来存secKillUser
 * @author:LSD
 * @when:2019/10/23/12:33
 */
public class SecKillUserContext {
    private static ThreadLocal<SecKillUser> userHolder = new ThreadLocal<SecKillUser>();

    public static void setSecKillUser(SecKillUser secKillUser) {
        userHolder.set(secKillUser);
    }
    public static SecKillUser getSecKillUser(){
        return userHolder.get();
    }
}
