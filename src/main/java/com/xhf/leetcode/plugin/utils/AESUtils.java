package com.xhf.leetcode.plugin.utils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    // AES加密算法
    private static final String ALGORITHM = "AES";
    // 密钥长度（128位、192位或256位）
    private static final int KEY_SIZE = 128;

    /**
     * 生成AES密钥
     *
     * @return 返回Base64编码后的密钥字符串
     */
    public static String generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen;
        keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 使用AES加密
     *
     * @param data 待加密的数据
     * @param key Base64编码后的密钥
     * @return 返回Base64编码后的加密数据
     */
    public static String encrypt(String data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 使用AES解密
     *
     * @param encryptedData Base64编码后的加密数据
     * @param key Base64编码后的密钥
     * @return 返回解密后的原始数据
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            // 生成AES密钥
            String secretKey = AESUtils.generateKey();
            System.out.println("生成的密钥: " + secretKey);

            // 原始数据
            String originalData = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImRqYW5nby5jb250cmliLmF1dGguYmFja2VuZHMuTW9kZWxCYWNrZW5kIiwiX2F1dGhfdXNlcl9oYXNoIjoiNzAwYmJmM2ZhOGQzMTNlNWJhY2EwN2NiODdiM2MyYWRjNTc4YTA2NzBkMDViODFmZDk1ZWFjNGE1N2JiZTllZiIsImlkIjozNzU2MDkwLCJlbWFpbCI6IiIsInVzZXJuYW1lIjoiYnUtY2h1YW4tbmVpLWt1LWQiLCJ1c2VyX3NsdWciOiJidS1jaHVhbi1uZWkta3UtZCIsImF2YXRhciI6Imh0dHBzOi8vYXNzZXRzLmxlZXRjb2RlLmNuL2FsaXl1bi1sYy11cGxvYWQvdXNlcnMvYnUtY2h1YW4tbmVpLWt1LWQvYXZhdGFyXzE3MTM1ODE1ODYucG5nIiwicGhvbmVfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6IjAzY2E3ZTQ4MWVkOTNlOWUwY2MyNDgxMDgyNTIzODFjIiwiaXAiOiIyMTEuOTMuMjQ4LjIxMiIsIl90aW1lc3RhbXAiOjE3NDA4MTU5NDEuNTg5NjAyLCJleHBpcmVkX3RpbWVfIjoxNzQzMzYxMjAwfQ.ZpGh4lftVnEJlxKg1HI8kp5J7Z07iojzdgdvH7nFO3U\\";
            System.out.println("原始数据: " + originalData);

            // 加密
            String encryptedData = AESUtils.encrypt(originalData, secretKey);
            System.out.println("加密后的数据: " + encryptedData);

            // 解密
            String decryptedData = AESUtils.decrypt(encryptedData, secretKey);
            System.out.println("解密后的数据: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}