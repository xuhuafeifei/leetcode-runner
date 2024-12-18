package com.xhf.leetcode.plugin.debug.analysis.convert;

public class IntMatrixConvertor implements VariableConvertor {
    @Override
    public String convert(String testcase, String variableName) {
        // 处理 int[][]
        StringBuilder sb = new StringBuilder();
        sb.append("int[][] ").append(variableName).append(" = {");
        
        // 去除外层的方括号并按行分割
        String[] rows = testcase.replace("[[", "").replace("]]", "").split("],\\s*\\[");
        
        for (int i = 0; i < rows.length; i++) {
            sb.append("{");
            // 按列分割
            String[] values = rows[i].split(",");
            for (int j = 0; j < values.length; j++) {
                sb.append(values[j].trim());
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
