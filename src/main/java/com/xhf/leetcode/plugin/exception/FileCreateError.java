package com.xhf.leetcode.plugin.exception;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class FileCreateError extends Throwable {

    public FileCreateError(String message) {
        super(message);
    }

    public FileCreateError(Throwable cause) {
        super(cause);
    }

    public FileCreateError(String message, Throwable cause) {
        super(message, cause);
    }
}
