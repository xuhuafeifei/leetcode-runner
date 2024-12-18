package com.xhf.leetcode.plugin.debug.params;

import com.xhf.leetcode.plugin.debug.params.parameter.*;

public enum Operation {
    /**
     * 单步运行, 默认为 1
     */
    N(new NParameterExtractor(), "^n\\s*(\\d+)?$"),
    /**
     * 运行至下一个断点
     */
    R(new RParameterExtractor(), "^r$"),
    /**
     * 打印当前栈中变量
     */
    P(new PParameterExtractor(), "^p\\s*(.*)?$"),
    /**
     * 断点
     */
    B(new BParameterExtractor(), "^b\\s*(\\d+)?$"),
    SHOWB(new SHOWBParameterExtractor(), "^show\\s+b$"),
    /**
     * 查看当前执行位置
     */
    W(new WParameterExtractor(), "^w$");

    // 每个操作对应的提取器
    private final ParameterExtractor parameterExtractor;
    // 每个操作对应的正则
    private final String regex;

    // 构造函数，初始化每个操作的参数提取策略
    Operation(ParameterExtractor parameterExtractor, String regex) {
        this.parameterExtractor = parameterExtractor;
        this.regex = regex;
    }

    // 获取该操作的参数提取策略
    public ParameterExtractor getParameterExtractor() {
        return parameterExtractor;
    }

    // 判断指令是否匹配当前操作
    public boolean matches(String inst) {
        String regex = getRegex();
        return inst != null && inst.matches(regex);
    }

    // 获取操作的正则表达式
    public String getRegex() {
        return this.regex;
    }
}
