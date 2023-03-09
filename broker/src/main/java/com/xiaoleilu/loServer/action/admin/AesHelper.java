package com.xiaoleilu.loServer.action.admin;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES加密解密帮助类
 */
public class AesHelper {

    /**
     * 加密类型名称
     */
    private final static String ENCRYPTED="AES";


    /**
     * AES加密字符串
     *
     * @param content
     *            需要被加密的字符串
     * @param password
     *            加密需要的密码
     * @return 密文
     */
    public static String encrypt(String content, String password) {
        try {
            // 创建AES的Key生产者
            KeyGenerator kgen = KeyGenerator.getInstance(ENCRYPTED);
            // 利用用户密码作为随机数初始化出
            kgen.init(128, new SecureRandom(password.getBytes()));
            // 128位的key生产者

            // 根据用户密码，生成一个密钥
            SecretKey secretKey = kgen.generateKey();

            // 返回基本编码格式的密钥，如果此密钥不支持编码，则返回null。
            byte[] enCodeFormat = secretKey.getEncoded();
            // 转换为AES专用密钥
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ENCRYPTED);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(ENCRYPTED);

            byte[] byteContent = content.getBytes("utf-8");

            // 初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // 加密
            byte[] result = cipher.doFinal(byteContent);
            //先将密文转换为16进制
            return AesParseSystemUtil.parseByte2HexStr(result);

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密AES加密过的字符串
     *
     * @param content
     *            AES加密过过的内容
     * @param password
     *            加密时的密码
     * @return 明文
     */
    public static String decrypt(String content, String password) {
        try {
            //将16进制转换为2进制
            byte[] contentByte= AesParseSystemUtil.parseHexStr2Byte(content);
            // 创建AES的Key生产者
            KeyGenerator kgen = KeyGenerator.getInstance(ENCRYPTED);
            kgen.init(128, new SecureRandom(password.getBytes()));
            // 根据用户密码，生成一个密钥
            SecretKey secretKey = kgen.generateKey();
            // 返回基本编码格式的密钥
            byte[] enCodeFormat = secretKey.getEncoded();
            // 转换为AES专用密钥
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ENCRYPTED);
            // 创建密码器
            Cipher cipher = Cipher.getInstance(ENCRYPTED);
            // 初始化为解密模式的密码器
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(contentByte);
            return new String(result);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String str = "192.168.2.156";
        String encrypt = encrypt(str, "yehuo");
        System.out.println(encrypt);
        String decrypt = decrypt(encrypt, "yehuo");
        System.out.println(decrypt);
    }
}
