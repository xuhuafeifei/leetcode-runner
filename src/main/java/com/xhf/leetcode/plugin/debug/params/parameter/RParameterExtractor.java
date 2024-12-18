package com.xhf.leetcode.plugin.debug.params.parameter;

public class RParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        // 'r' 没有参数，直接返回 null
        return null;
    }
}