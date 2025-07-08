package com.xhf.leetcode.plugin.listener;

import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.WatchPoolRemoveEvent;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;

/**
 * 为MyList提供右键菜单. 菜单包括copy, delete选项
 *
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
     *
     * @return 返回popupMenu
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        // 删除操作菜单项
        JMenuItem deleteItem = new JMenuItem(BundleUtils.i18nHelper("删除", "Delete"));
        deleteItem.addActionListener(e -> {
            // 删除多选项
            int[] selectedIndices = variableList.getSelectedIndices();

            // 从selectedIndices中删除指定索引的元素，并将剩余元素保存到新数组中
            for (int selectedIndex : selectedIndices) {
                ListModel<T> model = variableList.getModel();
                T removedElement = model.getElementAt(selectedIndex);

                // 类型安全过滤
                @SuppressWarnings("unchecked")
                T[] data = (T[]) new Object[model.getSize() - 1];
                for (int i = 0; i < model.getSize(); i++) {
                    if (i < selectedIndex) {
                        data[i] = model.getElementAt(i);
                    } else if (i > selectedIndex) {
                        data[i - 1] = model.getElementAt(i);
                    }
                }

                // 通知watch pool删除对应数据, 如果删除的是watch pool内的
                LCEventBus.getInstance().post(new WatchPoolRemoveEvent(removedElement.toString()));
                variableList.setListData(data);
            }
        });
        popupMenu.add(deleteItem);
        // 复制操作菜单项
        JMenuItem copyItem = new JMenuItem(BundleUtils.i18nHelper("复制", "Copy"));
        copyItem.addActionListener(e -> {
            int selectedIndex = variableList.getSelectedIndex();
            if (selectedIndex != -1) {
                String data = variableList.getModel().getElementAt(selectedIndex).toString();
                // 将数据转换为字符串复制到剪贴板
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
        // 检测是否为右键按下事件
        if (e.isPopupTrigger()) {
            showPopupMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // 检测是否为右键释放事件
        if (e.isPopupTrigger()) {
            showPopupMenu(e);
        }
    }

    private void showPopupMenu(MouseEvent e) {
        // 在鼠标位置显示弹出菜单
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}
