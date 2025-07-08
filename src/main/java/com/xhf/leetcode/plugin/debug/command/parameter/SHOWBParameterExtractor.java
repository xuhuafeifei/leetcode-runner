package com.xhf.leetcode.plugin.debug.command.parameter;

/**
 * SHOWB操作, 支持读取show b, s b, sb. 没必要解析出参数
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SHOWBParameterExtractor implements ParameterExtractor {

    @Override
    public String extract(String inst) {
        // return inst.trim().split(" ")[1];
        return "";
    }
}
