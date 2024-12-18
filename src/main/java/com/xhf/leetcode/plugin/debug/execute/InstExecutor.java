package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.params.Instrument;

/**
 * 指令执行器
 */
public interface InstExecutor {
    /**
     * 执行指令
     * @param inst 指令
     * @param context 指令执行时候的上下文
     * @return 是否执行成功
     */
    ExecuteResult execute(Instrument inst, Context context);
}
