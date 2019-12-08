package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/23/10:39
 */
public class AccessKey extends BasePrefix {

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public AccessKey(String prefix) {
        super(prefix);
    }

    public static AccessKey access = new AccessKey(5,"access");

    /**
     * 上面的access的expireSeconds是写死的，这里是自定义设置的
     * @param expireSeconds
     * @return
     */
    public static AccessKey withExpire(int expireSeconds){
        return new AccessKey(expireSeconds,"access");
    }
}
