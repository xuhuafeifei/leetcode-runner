package com.xhf.leetcode.plugin.debug.params;

import com.xhf.leetcode.plugin.debug.params.parameter.*;

public enum Operation {
    /**
     * 单步运行, 默认为 1
     * n 3 (当前版本只支持单步运行, 哪怕后续的数字参数为3, 也只会单步运行)
     */
    N(new NParameterExtractor(), "^n\\s*(\\d+)?$", "N"),
    /**
     * 运行至下一个断点 r
     * r
     */
    R(new RParameterExtractor(), "^r$", "R"),
    /**
     * 打印当前栈中变量 p
     * p
     */
    P(new PParameterExtractor(), "^p\\s*(.*)?$", "P"),
    /**
     * 断点 b [lineNumber]
     * b 2
     */
    B(new BParameterExtractor(), "^b\\s*(\\d+)?$", "B"),
    /**
     * 显示所有断点 [show|s] b
     *  show b
     *  s b
     *  sb
     */
    SHOWB(new SHOWBParameterExtractor(), "^(show|s)\\s*b$", "SHOWB"),
    /**
     * 移除断点 [remove|r] b [lineNumber]
     * remove b 1
     * r b 1
     * rb 1
     * remove b1
     * r b1
     * rb1
     * <p>
     * ^remove表示以remove开头
     * \s+表示匹配一个或多个空格
     * \s*表示匹配0个或多个空格
     * \d+表示匹配一个或多个数字
     * $表示结尾
     * |表示或
     * ?表示前面的表达式可以出现一次或多次
     */
    RB(new RBParameterExtractor(), "^remove\\s+b\\s*\\d+$|r\\s*b\\s*\\d+$", "RB"),
    /**
     * 移除所有断点
     * remove all
     * ra
     * r a
     */
    RBA(new RBAParameterExtractor(), "^remove\\s+(all|a)$|^r\\s*a$", "RBA"),
    /**
     * 查看当前执行位置 w
     * w
     */
    W(new WParameterExtractor(), "^w$", "W"),
    /**
     * step指令, 支持step over 和step out. 不支持缩写
     */
    STEP(new STEPParameterExtractor(), "^step\\s+[over|out]$", "STEP"),
    /**
     * 帮助指令
     */
    HELP(new HELPParameterExtractor(), "^help$|^h$", "HELP");

    // 每个操作对应的提取器
    private final ParameterExtractor parameterExtractor;
    // 每个操作对应的正则
    private final String regex;
    // 指令名称
    private final String name;

    // 构造函数，初始化每个操作的参数提取策略
    Operation(ParameterExtractor parameterExtractor, String regex, String name) {
        this.parameterExtractor = parameterExtractor;
        this.regex = regex;
        this.name = name;
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

    public String getName() {
        return name;
    }
}
