package com.xhf.leetcode.plugin.debug.execute.java.p;

import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.BLUE;
import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.GREEN;
import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.WHITE;
import static com.xhf.leetcode.plugin.debug.execute.java.p.Color.YELLOW;

import com.xhf.leetcode.plugin.debug.analysis.converter.convert.TreeNode;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TreeNodePrinter extends TreePrinter {

    private final Color folderColor = BLUE;
    private final Color hiddenFolderColor = YELLOW;
    private final Color executableFileColor = GREEN;
    private final Color nonExecutableFileColor = WHITE;

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
