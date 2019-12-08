package cn.andios.seckill.util;

import java.util.UUID;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/18:36
 */
public class UUIDUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
