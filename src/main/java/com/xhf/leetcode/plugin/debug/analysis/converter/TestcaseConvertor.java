package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.xhf.leetcode.plugin.exception.DebugError;

public interface TestcaseConvertor {

    /**
     * 自动获取当前打文件处理的question的测试样例, 并转换为对应的调用代码
     * @return
     */
    public String autoConvert() throws DebugError;

    /**
     * 处理多轮solution方法调用的testcases
     *
     * @param testcases
     * @return
     */
    @Deprecated // 不支持多轮处理, 要不然debug看不清输入了
    public String convert(String testcases);

    /**
     * 只负责处理一轮solution方法调用的testcases
     * 比如solution.twoSum(int, int). testcases只能有两个测试案例. 处理完成后, 则为一轮
     *
     * @param testCases
     * @return
     */
    public String convert(String[] testCases) throws DebugError;
}
