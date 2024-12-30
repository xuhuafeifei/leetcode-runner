package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.xhf.leetcode.plugin.debug.analysis.converter.convert.TreeNode;

import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.*;
import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.WHITE;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TreeNodePrinter extends TreePrinter {
    private Color folderColor = BLUE;
    private Color hiddenFolderColor = YELLOW;
    private Color executableFileColor = GREEN;
    private Color nonExecutableFileColor = WHITE;

    public TreeNodePrinter(Object root) {
        super(root);
    }

    @Override
    public Object[] getChild(Object obj) {
        TreeNode node = (TreeNode) obj;
        Object[] childs = new Object[2];
        int i = 0;
        childs[i++] = node.right == null ? null : node.right;
        childs[i++] = node.left == null ? null : node.left;
        return childs;
    }

    @Override
    public String getValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        TreeNode node = (TreeNode) obj;
        return String.valueOf(node.val);
    }

    @Override
    public boolean isLeaf(Object obj) {
        TreeNode node = (TreeNode) obj;
        if (node == null) {
            return true;
        }
        return node.left == null && node.right == null;
    }
}
