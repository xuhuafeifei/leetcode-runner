package com.xhf.leetcode.plugin.comp;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MyJTextAreaWithPopupMenu extends JTextArea {

    protected JPopupMenu popupMenu;

    public MyJTextAreaWithPopupMenu() {
        popupMenu = new JPopupMenu();
        // 添加鼠标监听器
        addMouseListener(new MouseAdapter() {
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
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void copy() {
        // copy to clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(this.getText());
        clipboard.setContents(stringSelection, null);
    }
}
