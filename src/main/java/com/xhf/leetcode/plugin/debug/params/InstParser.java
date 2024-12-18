package com.xhf.leetcode.plugin.debug.params;

/**
 * 指令解析器
 */
public interface InstParser {

    /**
     * 解析指令
     * @param inst 指令
     * @return 解析对象
     */
    Instrument parse(String inst) throws IllegalArgumentException;
}
