package com.xhf.leetcode.plugin.search;

public class Token {

    // 当前文本处理得到的token
    private String token;
    // 当前处理的字符长度(等于处理的字符次数)
    private int len;

    public Token() {
    }

    public Token(String token, int len) {
        this.token = token;
        this.len = len;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}