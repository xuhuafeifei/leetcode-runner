package com.xhf.leetcode.plugin.debug.params.parameter;

import com.xhf.leetcode.plugin.debug.params.parameter.ParameterExtractor;

public class PParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        // 提取 'p' 后的表达式，例如：p x + y
        String param = inst.replace("p", "").trim();
        return param.isEmpty() ? null : param;
    }
}
