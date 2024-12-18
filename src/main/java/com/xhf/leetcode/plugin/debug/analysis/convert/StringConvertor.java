package com.xhf.leetcode.plugin.debug.analysis.convert;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StringConvertor implements VariableConvertor {
    @Override
    public String convert(String testcase, String variableName) {
        return "String " + variableName + " = \"" + testcase + "\";\r\n";
    }
}
