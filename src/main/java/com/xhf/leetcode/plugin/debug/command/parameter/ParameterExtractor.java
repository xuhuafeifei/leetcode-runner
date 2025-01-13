package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * 指令参数提取者, 用于提取指令参数
 */
public interface ParameterExtractor {
    String extract(String inst);
}
