package com.xhf.leetcode.plugin.debug.env;

import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * 插件断点环境
 */
public interface DebugEnv {

    /**
     * 环境准备
     *
     * @return 是否准备完毕
     */
    boolean prepare() throws DebugError;

    boolean isDebug();

    void stopDebug();
}
