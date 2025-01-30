package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StringArrayConvertor extends AbstractVariableConvertor {
    @Override
    protected String doJava(String testcase, String variableName) {
        StringBuilder sb = new StringBuilder();
        // 处理 String[]
        sb.append("String[] ").append(variableName).append(" = new String[]{");
        return cLikeSuffix(testcase, sb);
    }

    @Override
    protected String doCpp(String testcase, String variableName) {
        StringBuilder sb = new StringBuilder();
        // 处理 vector<string>
        sb.append("vector<string> ").append(variableName).append(" = {");
        return cLikeSuffix(testcase, sb);
    }

    @NotNull
    private String cLikeSuffix(String testcase, StringBuilder sb) {
        testcase = testcase.replace("\"", "");
        String[] values = testcase.replace("[", "").replace("]", "").split(",");
        for (int i = 0; i < values.length; i++) {
            sb.append("\"").append(values[i].trim()).append("\"");
            if (i < values.length - 1) {
                sb.append(",");
            }
        }
        sb.append("};\r\n");
        return sb.toString();
    }

    @Override
    protected String doPython(String testcase, String variableName) {
        return TAB + variableName + " = " + testcase + "\r\n";
    }
}
