package com.xhf.leetcode.plugin.debug.reader;

import com.xhf.leetcode.plugin.debug.params.Instrument;

/**
 * 指令读取器. 从输入读取信息并解析为指令
 */
public interface InstReader {
    /**
     * 读取并解析为指令
     * @return inst
     */
    Instrument readInst();
}
