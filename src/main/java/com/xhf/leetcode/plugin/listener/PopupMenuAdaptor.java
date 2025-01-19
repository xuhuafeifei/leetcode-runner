package com.xhf.leetcode.plugin.listener;

import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.comp.MyList;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 为MyList提供右键菜单. 菜单包括copy, delete选项
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PopupMenuAdaptor<T> extends MouseAdapter {

    private final MyList<T> variableList;
    private final JPopupMenu popupMenu;

    public PopupMenuAdaptor(MyList<T> variableList) {
        this.variableList = variableList;
        this.popupMenu = createPopupMenu();
    }

    /**
     * 创建popupMenu
     * @return 返回popupMenu
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        // delete
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int[] selectedIndices = variableList.getSelectedIndices();
            for (int selectedIndex : selectedIndices) {
                ListModel<T> model = variableList.getModel();
                T[] data = (T[]) new Object[model.getSize() - 1];
                for (int i = 0; i < model.getSize(); i++) {
                    if (i < selectedIndex) {
                        data[i] = model.getElementAt(i);
                    }else if (i > selectedIndex) {
                        data[i - 1] = model.getElementAt(i);
                    }
                }
                // 通知watch pool删除对应数据, 如果删除的是watch pool内的
                LCEventBus.getInstance().post(new WatchPoolRemoveEvent(model.getElementAt(selectedIndex).toString()));
                variableList.setListData(data);
            }
        });
        popupMenu.add(deleteItem);
        // copy
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            int selectedIndex = variableList.getSelectedIndex();
            if (selectedIndex != -1) {
                String data = variableList.getModel().getElementAt(selectedIndex).toString();
                // copy to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection stringSelection = new StringSelection(data);
                clipboard.setContents(stringSelection, null);
            }
        });
        popupMenu.add(copyItem);
        // 返回menu
        return popupMenu;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopupMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopupMenu(e);
        }
    }

    private void showPopupMenu(MouseEvent e) {
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}
