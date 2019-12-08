package cn.andios.seckill.redis;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/13:48
 */
public class GoodsKey extends  BasePrefix{



    private GoodsKey(int expireSeconds,String prefix){
        super(expireSeconds,prefix);
    }
    private GoodsKey(String prefix){
        super(prefix);
    }

    public static GoodsKey getSecKillGoodsList = new GoodsKey(60,"goodsList");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"goodsDetail");
    public static GoodsKey getSecKillGoodsStock = new GoodsKey(0,"secKillGoodsStock");
    public static GoodsKey isGoodsSecKillOver = new GoodsKey("goodsSecKillOver");

}
