package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * 帮助指令不需要参数
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class HELPParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        return "";
    }
}
