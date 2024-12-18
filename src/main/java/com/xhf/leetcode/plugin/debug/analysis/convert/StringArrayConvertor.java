package com.xhf.leetcode.plugin.debug.analysis.convert;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StringArrayConvertor implements VariableConvertor {
    @Override
    public String convert(String testcase, String variableName) {
        StringBuilder sb = new StringBuilder();
        // 处理 String[]
        sb.append("String[] ").append(variableName).append(" = new String[]{");
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
}
