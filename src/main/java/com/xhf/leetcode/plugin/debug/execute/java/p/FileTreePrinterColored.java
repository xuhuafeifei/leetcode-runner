package com.xhf.leetcode.plugin.debug.execute.java.p;


import java.io.File;

import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.*;


/**
 * @author deviknitkkr
 * @link https://github.com/deviknitkk
 * 这个是我从github上看到的一个树形打印框架的代码，不过这个老哥是打印文件树, 而我是打印TreeNode
 * 因此把老哥抽象出来的TreePrinter copy过来, 结合我的项目需求稍微改改
 */
public class FileTreePrinterColored extends FileTreePrinter {
    private Color folderColor = BLUE;
    private Color hiddenFolderColor = YELLOW;
    private Color executableFileColor = GREEN;
    private Color nonExecutableFileColor = WHITE;

    public FileTreePrinterColored(Object root) {
        super(root);
    }

    @Override
    public String getValue(Object obj) {
        File file = (File) obj;
        if (file.isDirectory()) {

            if (file.isHidden())
                return hiddenFolderColor + super.getValue(obj) + RESET;

            return folderColor + super.getValue(obj) + RESET;

        } else if (file.canExecute())
            return executableFileColor + super.getValue(obj) + RESET;

        else
            return nonExecutableFileColor + super.getValue(obj) + RESET;
    }

    public void setFolderColor(Color folderColor) {
        this.folderColor = folderColor;
    }

    public void setHiddenFolderColor(Color hiddenFolderColor) {
        this.hiddenFolderColor = hiddenFolderColor;
    }

    public void setExecutableFileColor(Color executableFileColor) {
        this.executableFileColor = executableFileColor;
    }

    public void setNonExecutableFileColor(Color nonExecutableFileColor) {
        this.nonExecutableFileColor = nonExecutableFileColor;
    }
}