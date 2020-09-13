package cn.edu.fudan.accountservice.util;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * description: base64
 *
 * @author Richy
 * create: 2020-09-11 13:52
 **/
public class Base64Util {

    /**
     * Base64加密
     * @param password
     * @return string
     */
    public static String encodePassword(String password) {
        byte[] encodePassword = Base64.getEncoder().encode(password.getBytes(StandardCharsets.UTF_8));
        return new String(encodePassword);
    }

    /**
     * Base64解码
     * @param encodePassword
     * @return string
     */
    public static String decodePassword(String encodePassword) {
        byte[] decodePassword = Base64.getDecoder().decode(encodePassword.getBytes());
        return new String(decodePassword);
    }

    public static void main(String[] args) {
        String admin = "admin";
        System.out.println(encodePassword(admin));
    }

}