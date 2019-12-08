package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/13:48
 */
public class SecKillUserKey extends  BasePrefix{

    public static final int TOKEN_EXPIRE = 3600*24;

    private SecKillUserKey(int expireSeconds, String prefix){
        super(expireSeconds,prefix);
    }
    public static SecKillUserKey token = new SecKillUserKey(TOKEN_EXPIRE,"token");
    public static SecKillUserKey getById = new SecKillUserKey(0,"id");
}
