package com.xhf.leetcode.plugin.debug.output;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdOutput implements Output{
    @Override
    public void output(String output) {
        System.out.println(output);
    }
}
