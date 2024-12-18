package com.xhf.leetcode.plugin.debug.params.parameter;

public class NParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        // 提取数字参数，例如：n 5
        String param = inst.replace("n", "").trim();
        return param.isEmpty() ? "1" : param;
    }
}