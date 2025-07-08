package com.xhf.leetcode.plugin.utils;

/**
 * 每日一题图标刷新状态枚举
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public enum TodayIconStatusEnum {
    NO_NEED_MODIFY(0, BundleUtils.i18nHelper("无需修改", "No need to modify")),
    SOLVED(1, BundleUtils.i18nHelper("已解决", "Need to modify")),
    NOT_SOLVED(2, BundleUtils.i18nHelper("未解决", "Not solved"));

    private final int code;
    private final String desc;

    TodayIconStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
