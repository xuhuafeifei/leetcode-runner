package ui;

import com.intellij.ui.treeStructure.SimpleTree;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimpleTreeWithPopup {

    private SimpleTree tree;

    public JComponent createTreePanel() {
        // 创建根节点和子节点
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Child 1");
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("Child 2");

        // 添加子节点到根节点
        root.add(child1);
        root.add(child2);

        // 给子节点添加孙节点
        child1.add(new DefaultMutableTreeNode("Grandchild 1"));
        child2.add(new DefaultMutableTreeNode("Grandchild 2"));

        // 创建 TreeModel 并与根节点关联
        DefaultTreeModel model = new DefaultTreeModel(root);

        // 创建 SimpleTree 并设置其模型
        tree = new SimpleTree(model);

        // 设置选择模式为单选
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // 添加鼠标监听器来处理弹出窗口
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int x = e.getX();
                    int y = e.getY();
                    TreePath path = tree.getPathForLocation(x, y);
                    if (path != null) {
                        JPopupMenu popup = createPopup(path.getLastPathComponent());
                        popup.show(e.getComponent(), x, y);
                    }
                }
            }
        });

        // 返回包含 SimpleTree 的滚动面板
        return new JScrollPane(tree);
    }

    private JPopupMenu createPopup(Object component) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Details about " + component.toString());
        popupMenu.add(menuItem);
        return popupMenu;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SimpleTree with Popup Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            SimpleTreeWithPopup demo = new SimpleTreeWithPopup();
            frame.add(demo.createTreePanel(), BorderLayout.CENTER);
            frame.setSize(400, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}