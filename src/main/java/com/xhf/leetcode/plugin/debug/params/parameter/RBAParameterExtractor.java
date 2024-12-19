package com.xhf.leetcode.plugin.debug.params.parameter;

/**
 * remove all无需解析出参数
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RBAParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        return "";
    }
}
