package com.xhf.leetcode.plugin.bus;

/**
 * deep coding 选择tab事件, 该事件会存储需要打开的tab 名称
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DeepCodingTabChooseEvent {

    private final String pattern;

    public DeepCodingTabChooseEvent(String pattern) {
        // 当前deep coding 的打开模式
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
