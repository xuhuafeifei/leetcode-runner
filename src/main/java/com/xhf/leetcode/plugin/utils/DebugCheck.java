package com.xhf.leetcode.plugin.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * debug状态检查, 该注解表明action需要处于debug状态才能运行
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DebugCheck {
    // 创建一个枚举类, 枚举检测的状态
    public enum CheckType {
        /**
         * 检测设置是否存(必须检测)
         */
        @Deprecated // 必须检测, 目前没有设置的必要性
        SETTING,
        /**
         * 检测是否处于debug状态
         */
        STATUS
    }
    CheckType value() default CheckType.SETTING;
}
