package com.xhf.leetcode.plugin.debug.analysis.convert;

/**
 * 将测试案例转换为变量, 并以对应语言的方式输出字符串
 * <p>
 * 为了避免类爆炸, VariableConvertor不区分语言. 这意味着每一个convertor都可以处理被允许debug的语言
 */
public interface VariableConvertor {
    String convert(String testcase, String variableName);
}
