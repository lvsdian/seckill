package cn.andios.seckill.util;

import cn.andios.seckill.vo.VerifyCodeVo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @description: 数学验证码,比如随机生成一个表达式：1+2-3，需要把result和bufferedImage对象返回,
 * result需要返回到SecKillService中存入Redis,bufferedImage需要返回到SecKillController中，再传到前端页面，
 * 所以创建了一个类VerifyCodeVo
 * @author:LSD
 * @when:2019/10/23/16:30
 */
public class VerifyCodeUtil {

    public static VerifyCodeVo createVerifyCode (){
        int width = 80,height = 32;
        //create the image
        BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        //set the background color
        graphics.setColor(new Color(0xDCDCDC));
        graphics.fillRect(0,0,width,height);
        //draw the border
        graphics.setColor(Color.black);
        graphics.drawRect(0,0,width - 1,height - 1);
        //create a random instance to generate the codes
        Random random = new Random();
        //make some confusion
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            graphics.drawOval(x,y,0,0);
        }
        //generate a random code'
        String verifyCode = generateVerifyCode(random);
        graphics.setColor(new Color(0,100,0));
        graphics.setFont(new Font("Candara",Font.BOLD,24));
        graphics.drawString(verifyCode,8,24);
        graphics.dispose();

        Integer result = calc(verifyCode);


        return new VerifyCodeVo(result,bufferedImage);
    }

    /**
     * 加减乘操作符数组
     */
    private static char[] opts = new char[]{'+','-','*'};

    /**
     * 生成字符串，比如："1-3+3"
     * @param random
     * @return
     */
    private static String generateVerifyCode(Random random) {
        int num1 = random.nextInt(10);
        int num2 = random.nextInt(10);
        int num3 = random.nextInt(10);
        char opt1 = opts[random.nextInt(3)];
        char opt2 = opts[random.nextInt(3)];
        return "" + num1 + opt1 + num2 + opt2 + num3;
    }

    /**
     * 计算字符串表达式的值
     * @param verifyCode 1-3+4
     * @return 2
     */
    private static int calc(String verifyCode) {
        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("javascript");
            return (Integer)scriptEngine.eval(verifyCode);
        } catch (ScriptException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
