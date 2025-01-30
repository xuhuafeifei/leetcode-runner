package com.xhf.leetcode.plugin.debug.analysis.converter.convert;

import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * {@link ParamType#LIST_STRING}
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ListStringConvertor extends AbstractVariableConvertor {
    @Override
    protected String doJava(String testcase, String variableName) {
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

    @Override
    protected String doCpp(String testcase, String variableName) {
        throw new DebugError("c++不支持LIST_STRING类型的测试案例转换!");
    }

    @Override
    protected String doPython(String testcase, String variableName) {
        throw new UnsupportedOperationException("Python does not support List<String>");
    }
}
