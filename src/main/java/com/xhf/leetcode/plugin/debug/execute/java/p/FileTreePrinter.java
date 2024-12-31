package com.xhf.leetcode.plugin.debug.execute.java.p;

import java.io.File;
import java.io.IOException;

/**
 * @author deviknitkkr
 * @link https://github.com/deviknitkk
 * 这个是我从github上看到的一个树形打印框架的代码，不过这个老哥是打印文件树, 而我是打印TreeNode
 * 因此把老哥抽象出来的TreePrinter copy过来, 结合我的项目需求稍微改改
 */
public class FileTreePrinter extends TreePrinter {

    public FileTreePrinter(Object root) {
        super(root);
    }

    @Override
    public Object[] getChild(Object obj) {
        File file=(File)obj;
        return file.listFiles();
    }

    @Override
    public String getValue(Object obj) {
        try {
            return ((File)obj).getCanonicalFile().getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ((File)obj).getName();
    }

    @Override
    public boolean isLeaf(Object obj) {
        return ((File)obj).isFile();
    }

    public static void main(String[] args) {
        System.out.println(new FileTreePrinter(new File("E:\\java_code\\leetcode-runner\\src\\main\\resources")).visitAndReturn());
    }
}