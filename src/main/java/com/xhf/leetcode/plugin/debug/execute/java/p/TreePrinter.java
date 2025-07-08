package com.xhf.leetcode.plugin.debug.execute.java.p;

/**
 * @author deviknitkkr
 * @link https://github.com/deviknitkk
 * 这个是我从github上看到的一个树形打印框架的代码，不过这个老哥是打印文件树, 而我是打印TreeNode
 * 因此把老哥抽象出来的TreePrinter copy过来, 结合我的项目需求稍微改改
 */
public abstract class TreePrinter {

    private final Object root;
    private StringBuilder sb;
    private final String pointerFotrLast = "└─";
    private final String pointerForOther = "├─";
    private boolean isInstantPrintNeeded;

    public TreePrinter(Object root) {
        this.root = root;
    }

    public abstract Object[] getChild(Object obj);

    public abstract String getValue(Object obj);

    public abstract boolean isLeaf(Object obj);

    public void visitAndPrint() {
        isInstantPrintNeeded = true;
        visitRoot();
    }

    public StringBuilder visitAndReturn() {
        isInstantPrintNeeded = false;
        sb = new StringBuilder();
        visitRoot();
        return this.sb;
    }

    private void visitRoot() {
        if (isInstantPrintNeeded) {
            instantPrint(root, "", "");
        } else {
            appendToSb(root, "", "");
        }

        if (isLeaf(root)) {
            return;
        }

        Object[] childs = getChild(root);
        for (int i = 0; i < childs.length; i++) {
            if (i == childs.length - 1) {
                visitNode(childs[i], "", pointerFotrLast, false);
            } else {
                visitNode(childs[i], "", pointerForOther, true);
            }
        }
    }

    private void visitNode(Object node, String padding, String pointer, boolean hasMore) {
        if (isInstantPrintNeeded) {
            instantPrint(node, padding, pointer);
        } else {
            appendToSb(node, padding, pointer);
        }

        if (isLeaf(node)) {
            return;
        }

        StringBuilder paddingBuilder = new StringBuilder(padding);
        paddingBuilder.append(hasMore ? "│  " : "   ");

        Object[] childs = getChild(node);
        for (int i = 0; i < childs.length; i++) {
            if (i == childs.length - 1) {
                visitNode(childs[i], paddingBuilder.toString(), pointerFotrLast, false);
            } else {
                visitNode(childs[i], paddingBuilder.toString(), pointerForOther, true);
            }
        }
    }

    private void instantPrint(Object obj, String padding, String pointer) {
        System.out.println(padding + pointer + getValue(obj));
    }

    private void appendToSb(Object obj, String padding, String pointer) {
        sb.append("\n" + padding + pointer + getValue(obj));
    }
}