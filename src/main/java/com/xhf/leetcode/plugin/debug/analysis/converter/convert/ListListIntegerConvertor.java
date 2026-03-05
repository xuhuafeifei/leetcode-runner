package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;

public class ListListIntegerConvertor extends AbstractVariableConvertor {

    @Override
    protected String doCpp(String testcase, String variableName) {
        throw new NotSupportedException("cpp not support to convert List_List_Integer! test case = " + testcase);
    }

    @Override
    protected String doPython(String testcase, String variableName) {
        throw new NotSupportedException("python not support to convert List_List_Integer! test case = " + testcase);
    }

    @Override
    protected String doJava(String testcase, String variableName) {
        StringBuilder sb = new StringBuilder();
        String[] rows = testcase.replace("[[", "").replace("]]", "").split("],\\s*\\[");

        sb.append("List<List<Integer>> ").append(variableName).append(" = new ArrayList<>();\n");

        for (String row : rows) {
            // 按列分割
            String[] values = row.split(",");
            sb.append(variableName).append(".add(").append("Arrays.asList(");
            for (int j = 0; j < values.length; j++) {
                sb.append(values[j].trim());
                if (j < values.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("));\n");
        }
        // 返回构建好的Java代码字符串
        return sb.toString();
    }
}
