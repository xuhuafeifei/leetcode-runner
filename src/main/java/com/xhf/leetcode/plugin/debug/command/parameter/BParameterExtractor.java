package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class BParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        // 提取数字参数，例如：b 5
        return inst.replace("b", "").trim();
    }
}
