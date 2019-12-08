package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/22/20:51
 */
public class SecKillKey extends BasePrefix {
    public SecKillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public SecKillKey(String prefix) {
        super(prefix);
    }

    public static SecKillKey getSecKillPath = new SecKillKey(60,"secKillPath");
    public static SecKillKey getSecKillVerifyCode = new SecKillKey(300,"secKillVerifyCode");
}
