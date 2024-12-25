package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StringConvertor extends AbstractVariableConvertor {
    @Override
    protected String doPython(String testcase, String variableName) {
        return TAB + variableName + " = " + testcase + "\r\n";
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        testcase = testcase.replace("\"", "");
        return "String " + variableName + " = \"" + testcase + "\";\r\n";
    }
}
