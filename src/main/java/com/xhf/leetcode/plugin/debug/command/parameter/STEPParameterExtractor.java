package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * 提取STEP参数, over或者out
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class STEPParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        return inst.trim().split("\\s+")[1];
    }
}
