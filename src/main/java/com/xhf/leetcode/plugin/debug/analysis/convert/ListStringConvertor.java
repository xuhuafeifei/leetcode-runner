package com.xhf.leetcode.plugin.debug.analysis.convert;

import java.util.Arrays;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ListStringConvertor implements VariableConvertor {
    @Override
    public String convert(String testcase, String variableName) {
        // 去除引号并替换方括号为大括号
        String modifiedTestcase = testcase
                .replace("[", "{")
                .replace("]", "}");

        // 使用 StringBuilder 拼接结果，生成可修改的 List

        return "List<String> " +
                variableName +
                " = new ArrayList<>(Arrays.asList(new String[]" +
                modifiedTestcase +
                "));\n";
    }
}
