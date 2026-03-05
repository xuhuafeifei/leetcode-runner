package com.xhf.leetcode.plugin.exception;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class IllegalOperationException extends Throwable {

    public IllegalOperationException(String msg) {
        super(msg, null);
    }

    public IllegalOperationException(String msg, Throwable e) {
        super(msg, e);
    }
}
