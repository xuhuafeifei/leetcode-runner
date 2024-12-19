package com.xhf.leetcode.plugin.debug.output;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdOutput implements Output{
    @Override
    public void output(String output) {
        // 检测末尾是否是换行
        if (!output.endsWith("\n")) {
            output += "\n";
        }
        System.out.println(output);
    }
}
