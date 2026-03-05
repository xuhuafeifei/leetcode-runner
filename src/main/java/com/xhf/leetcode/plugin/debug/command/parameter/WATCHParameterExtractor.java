package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class WATCHParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        String param = inst.replaceFirst("watch", "").trim();
        return param.isEmpty() ? null : param;
    }
}
