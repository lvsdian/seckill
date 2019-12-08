package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/13:48
 */
public class UserKey extends  BasePrefix{

    public static final int TOKEN_EXPIRE = 3600;


    private UserKey(int expireSeconds,String prefix){
        super(expireSeconds,prefix);
    }

    public static UserKey getById = new UserKey(TOKEN_EXPIRE,"id");

}
