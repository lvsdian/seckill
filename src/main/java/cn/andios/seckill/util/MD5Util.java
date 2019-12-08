package cn.andios.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @description:
 * @author:LSD
 * @when:2019/10/15/15:11
 */
public class MD5Util {
    /**
     * md5加密
     * @param src 要加密的字符串
     * @return
     */
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    /**
     * 客户端固定的字符串
     */
    public static final String SALT = "1a2b3c";

    /**
     * 将客户端明文密码根据固定盐值加密
     * @param inputPass 用户的明文密码
     * @return 第一次加密后的字符串
     */
    public static String inputPassToFormPass(String inputPass,String salt){
        String str = ""+salt.charAt(1) + salt.charAt(2)+ inputPass +salt.charAt(3)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 将客户端加密密码根据随机盐值加密成服务器端密码
     * @param formPass  客户端加密的密码
     * @param salt  随机盐值
     * @return 第二次加密后的字符串
     */
    public static String formPassToDBPass(String formPass,String salt){
        String str = ""+salt.charAt(1) + salt.charAt(2) + formPass + salt.charAt(3)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 用户输入密码加密成服务器密码
     * @param inputPass 用户输入密码
     * @param dbSalt    盐值
     * @return 加密后的密码ef279a3d1f98977851e3a1c0f9186653
     */
    public static String inputPassToDbPass(String inputPass,String dbSalt){
        String formPass = inputPassToFormPass(inputPass,SALT);
        return formPassToDBPass(formPass,dbSalt);
    }

    public static void main(String[] args) {
        System.out.println(formPassToDBPass("ef279a3d1f98977851e3a1c0f9186653","1a2b3c"));
    }
}
