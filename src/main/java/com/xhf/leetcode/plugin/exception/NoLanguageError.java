package com.xhf.leetcode.plugin.exception;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class NoLanguageError extends Throwable {

    public NoLanguageError(String s) {
        super(s);
    }

    public NoLanguageError(String s, Throwable cause) {
        super(s, cause);
    }
}
