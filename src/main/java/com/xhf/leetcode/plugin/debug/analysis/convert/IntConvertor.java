package com.xhf.leetcode.plugin.debug.analysis.convert;

import com.xhf.leetcode.plugin.debug.analysis.convert.VariableConvertor;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class IntConvertor implements VariableConvertor {

    @Override
    public String convert(String testcase, String variableName) {
        // 处理 int
        return "int " + variableName + " = " + testcase + ";\r\n";
    }
}
