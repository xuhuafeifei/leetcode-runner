package com.xhf.leetcode.plugin.debug.execute.java.p;

/**
 * @author deviknitkkr
 * @link https://github.com/deviknitkk
 * 这个是我从github上看到的一个树形打印框架的代码，不过这个老哥是打印文件树, 而我是打印TreeNode
 * 因此把老哥抽象出来的TreePrinter copy过来, 结合我的项目需求稍微改改
 */
public enum Color {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\033[0;94m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m");

    private String color;

    Color(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color;
    }
}