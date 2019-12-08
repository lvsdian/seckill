package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/13:48
 */
public class SecKillOrderKey extends  BasePrefix{



    private SecKillOrderKey(int expireSeconds, String prefix){

        super(expireSeconds,prefix);
    }
    private SecKillOrderKey(String prefix){
        super(prefix);
    }

    public static SecKillOrderKey getSecKillOrderByUserIdGoodsId = new SecKillOrderKey("secKillOrderByUserIdGoodsId");

}
