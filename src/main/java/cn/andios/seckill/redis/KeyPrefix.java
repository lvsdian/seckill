package cn.andios.seckill.redis;

/**
 * @description:redis的key的前缀
 * @author:LSD
 * @when:2019/10/15/13:42
 */
public interface KeyPrefix {
    /**
     * 过期时间
     * @return
     */
    int getExpireSeconds();

    /**
     * 获取前缀
     * @return
     */
    String getPrefix();
}
