package com.xhf.leetcode.plugin.debug.reader;

import java.util.Scanner;

/**
 * 从标准输入读取指令
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StdInReader implements InstReader{
    @Override
    public String readInst() {
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        // break的意思
        if (line.equals("bk")) {
            return null;
        }
        return line;
    }
}
