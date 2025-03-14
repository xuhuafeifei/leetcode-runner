package com.xhf.leetcode.plugin.model;

/**
 * @author 文艺倾年
 */
public class UserStatus {
    /**
     * 是否登录
     */
    boolean isSignedIn;

    /**
     * 是否是会员
     */
    boolean isPremium;

    /**
     * 用户名
     */
    String username;

    /**
     * 用户真实名
     */
    String realName;


    public boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(boolean premium) {
        isPremium = premium;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public boolean getIsSignedIn() {
        return isSignedIn;
    }

    public void setIsSignedIn(boolean signedIn) {
        isSignedIn = signedIn;
    }
}
