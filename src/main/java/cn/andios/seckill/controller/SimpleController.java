package cn.andios.seckill.controller;

import cn.andios.seckill.domain.User;
import cn.andios.seckill.rabbitmq.MQSender;
import cn.andios.seckill.redis.RedisService;
import cn.andios.seckill.redis.UserKey;
import cn.andios.seckill.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/14/13:17
 */
@Controller
@RequestMapping("/simple")
public class SimpleController {


    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisService redisService;

    @RequestMapping("/demo")
    public String simple(Model model){
        model.addAttribute("name","seckill");
        return "hello";
    }

    @RequestMapping("/redis/setGetPrefix")
    @ResponseBody
    public Result<User> setGetPrefix(){
        User user1 = new User(1,"123456");
        redisService.set(UserKey.getById,"" + 1,user1);
        User user2 = redisService.get(UserKey.getById,"_" + 1,User.class);
        return Result.success(user2);
    }

    @RequestMapping("/mq_direct")
    @ResponseBody
    public Result<String> mq1(){
        mqSender.sendDirect("hello_direct");
        return Result.success("hello_direct,world_direct");
    }

    @RequestMapping("/mq_topic")
    @ResponseBody
    public Result<String> mq2(){
        mqSender.sendTopic("hello_topic");
        return Result.success("hello_topic,world_topic");
    }

    @RequestMapping("/mq_fanout")
    @ResponseBody
    public Result<String> mq3(){
        mqSender.sendFanout("hello_fanout");
        return Result.success("hello_fanout,world_fanout");
    }

    @RequestMapping("/mq_headers")
    @ResponseBody
    public Result<String> mq4(){
        mqSender.sendHeaders("hello_headers");
        return Result.success("hello_headers,world_headers");
    }

}
