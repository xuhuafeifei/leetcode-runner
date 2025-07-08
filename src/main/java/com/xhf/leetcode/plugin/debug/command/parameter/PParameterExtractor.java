package com.xhf.leetcode.plugin.debug.command.parameter;

public class PParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        // 提取 'p' 后的表达式，例如：p x + y(做了个预留拓展, 目前不针对表达式做额外处理)
        String param = inst.replaceFirst("p", "").trim();
        return param.isEmpty() ? null : param;
    }
}
