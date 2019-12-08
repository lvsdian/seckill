package cn.andios.seckill.service;

import cn.andios.seckill.dao.GoodsDao;
import cn.andios.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/16/14:21
 */
@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    public List<GoodsVo> listSecKillGoodsVo(){
        return goodsDao.listSecKillGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(Long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {
        int ret = goodsDao.reduceStock(goodsVo.getId());
        return ret > 0;
    }
}
