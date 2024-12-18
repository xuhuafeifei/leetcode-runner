package com.xhf.leetcode.plugin.debug.analysis.convert;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class IntArrayConvertor implements VariableConvertor{
    @Override
    public String convert(String testcase, String variableName) {
        // 处理 int[]
        StringBuilder sb = new StringBuilder();
        sb.append("int[] ").append(variableName).append(" = new int[]{");
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
}
