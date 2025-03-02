package com.xhf.leetcode.plugin.bus;

/**
 * 密钥更改事件
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SecretKeyUpdateEvent {
    private final String secretKey;

    public SecretKeyUpdateEvent(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
