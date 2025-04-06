package com.xhf.leetcode.plugin.exception;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LoginError extends Throwable {
    public LoginError(String message) {
        super(message);
    }

    public LoginError(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginError(Throwable cause) {
        super(cause);
    }
}
