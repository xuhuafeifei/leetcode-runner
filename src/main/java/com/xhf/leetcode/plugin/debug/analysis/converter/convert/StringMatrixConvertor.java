package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import org.jetbrains.annotations.NotNull;

/**
 * {@link ParamType#String_MATRIX}
 */
public class StringMatrixConvertor extends AbstractVariableConvertor {

    @Override
    protected String doCpp(String testcase, String variableName) {
        // 处理 vector<vector<string>>
        StringBuilder sb = new StringBuilder();
        sb.append("vector<vector<string>> ").append(variableName).append(" = {");

        return cLikeSuffix(testcase, sb);
    }

    @Override
    protected String doPython(String testcase, String variableName) {
        return TAB + variableName + " = " + testcase + "\r\n";
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        // 处理 String[][]
        StringBuilder sb = new StringBuilder();
        sb.append("String[][] ").append(variableName).append(" = {");

        return cLikeSuffix(testcase, sb);
    }

    @NotNull
    private String cLikeSuffix(String testcase, StringBuilder sb) {
        testcase = testcase.replace("\"", "");
        // 去除外层的方括号并按行分割
        String[] rows = testcase.replace("[[", "").replace("]]", "").split("],\\s*\\[");

        for (int i = 0; i < rows.length; i++) {
            sb.append("{");
            // 按列分割
            String[] values = rows[i].split(",");
            for (int j = 0; j < values.length; j++) {
                sb.append("\"").append(values[j].trim()).append("\"");
                if (j < values.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
            if (i < rows.length - 1) {
                sb.append(",");
            }
        }

        sb.append("};\r\n");
        return sb.toString();
    }
}
