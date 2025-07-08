package com.xhf.leetcode.plugin.debug.debugger;

import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * 核心debug类, 负责启动整个debugger框架
 */
public interface Debugger {

    /**
     * 启动debugger
     */
    void start();

    /**
     * 终止debugger
     * 尽量通过调用DebugManager.stop()停止debugger
     */
    void stop();

    void doFailed(Instruction inst);

    DebugEnv getEnv();
}
