package com.xhf.leetcode.plugin.debug.env;

import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * 插件断点环境
 */
public interface DebugEnv {
    /**
     * 环境准备
     * @return 是否准备完毕
     */
    boolean prepare() throws DebugError;

    /**
     * 是否处于debug状态
     * @return true:是
     */
    boolean isDebug();

    /**
     * 终止debug
     */
    void stopDebug();

    /**
     * 开始debug
     */
    void startDebug();

    /**
     * 获取bean
     */
    Class<?> getBean(Class<?> clazz);
}
