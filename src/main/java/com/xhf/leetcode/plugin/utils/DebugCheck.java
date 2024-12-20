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
}
