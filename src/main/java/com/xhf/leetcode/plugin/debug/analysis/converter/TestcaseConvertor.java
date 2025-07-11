package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.xhf.leetcode.plugin.exception.DebugError;

/**
 * 测试案例转换器, 可以将测试案例转换为对应语言的代码
 */
public interface TestcaseConvertor {

    /**
     * 自动获取当前打文件处理的question的测试样例, 并转换为对应的调用代码
     *
     * @return string
     */
    String autoConvert() throws DebugError;

    /**
     * 只负责处理一轮solution方法调用的testcases
     * 比如solution.twoSum(int, int). testcases只能有两个测试案例. 处理完成后, 则为一轮
     *
     * @param testCases 测试案例
     * @return 转换后的代码
     */
    String convert(String[] testCases) throws DebugError;
}
