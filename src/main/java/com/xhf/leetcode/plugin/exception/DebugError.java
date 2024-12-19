package com.xhf.leetcode.plugin.exception;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugError extends RuntimeException{
    public DebugError(String message) {
        super(message, null);
    }

    public DebugError(String message, Throwable e) {
        super(message, e);
    }
}
