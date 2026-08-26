package com.xhf.leetcode.plugin.io.http;

/**
 * LeetCode 请求异常分类
 */
public abstract class LcException extends RuntimeException {

    private LcException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 超时 */
    public static class Timeout extends LcException {
        public Timeout(Throwable cause) {
            super("请求超时", cause);
        }
    }

    /** 网络错误 */
    public static class Network extends LcException {
        public Network(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** 未授权 */
    public static class Unauthorized extends LcException {
        public Unauthorized(String message) {
            super(message, null);
        }
    }

    /** 业务错误 */
    public static class Business extends LcException {
        public Business(String message) {
            super(message, null);
        }
    }

    /** 未知错误 */
    public static class Unknown extends LcException {
        public Unknown(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
