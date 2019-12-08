package cn.andios.seckill.controller;

import cn.andios.seckill.domain.Order;
import cn.andios.seckill.domain.SecKillUser;
import cn.andios.seckill.result.CodeMsg;
import cn.andios.seckill.result.Result;
import cn.andios.seckill.service.GoodsService;
import cn.andios.seckill.service.OrderService;
import cn.andios.seckill.vo.GoodsVo;
import cn.andios.seckill.vo.OrderDetailVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/19/20:03
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    private static Logger logger = LoggerFactory.getLogger(SecKillController.class);

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> orderInfo(Model model, SecKillUser secKillUser,
                                    @RequestParam("orderId")Long orderId){
        if(secKillUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        logger.info("oderId："+orderId);
        Order order = orderService.getOrderByOrderId(orderId);
        if(order == null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrder(order);

        return Result.success(orderDetailVo);
    }
}
