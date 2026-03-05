package com.xhf.leetcode.plugin.exception;

/**
 * 计算表达式出现的错误
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ComputeError extends RuntimeException {

    public ComputeError(String message) {
        super(message, null);
    }

    public ComputeError(String message, Throwable e) {
        super(message, e);
    }
}
