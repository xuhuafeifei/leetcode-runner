package com.xhf.leetcode.plugin.debug.params.parameter;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SHOWBParameterExtractor implements ParameterExtractor {
    @Override
    public String extract(String inst) {
        return inst.trim().split(" ")[1];
    }
}
