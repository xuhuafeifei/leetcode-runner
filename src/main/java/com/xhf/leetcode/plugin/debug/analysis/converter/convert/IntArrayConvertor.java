package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class IntArrayConvertor extends AbstractVariableConvertor {
    @Override
    protected String doCpp(String testcase, String variableName) {
        // 处理 vector<int>
        StringBuilder sb = new StringBuilder();
        sb.append("vector<int> ").append(variableName).append(" = {");
        return cLikeSuffix(testcase, sb);
    }

    /**
     * 类c语言处理后缀
     * @param testcase testcase
     * @param sb sb
     * @return String
     */
    @NotNull
    private String cLikeSuffix(String testcase, StringBuilder sb) {
        String[] values = testcase.replace("[", "").replace("]", "").split(",");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
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

    @Override
    protected String doJava(String testcase, String variableName) {
        // 处理 int[]
        StringBuilder sb = new StringBuilder();
        sb.append("int[] ").append(variableName).append(" = new int[]{");
        return cLikeSuffix(testcase, sb);
    }
}
