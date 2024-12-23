package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LangType;

import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class IntConvertor extends AbstractVariableConvertor {
    @Override
    protected String doPython(String testcase, String variableName) {
        return TAB + variableName + " = " + testcase + "\r\n";
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        return "int " + variableName + " = " + testcase + ";\r\n";
    }
}
