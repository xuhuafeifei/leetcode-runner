package com.xhf.leetcode.plugin.utils;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class HotKeyUtils {

    // 通用方法：绑定键盘热键并执行移动操作
    public static void bindKey(JComponent panel, String keyStroke, String actionName, Action act) {
        // 绑定键盘事件
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyStroke), actionName);

        // 设置对应的动作
        panel.getActionMap().put(actionName, act);
    }

    // 通用方法：为每个 keyCode 绑定不同的操作
    public static void bindKey(JComponent panel, int keyCode, String actionName, Action act) {
        // 绑定键盘事件
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), actionName);

        // 设置对应的动作
        panel.getActionMap().put(actionName, act);
    }


    // 通用方法：绑定带修饰符的键盘热键
    public static void bindKey(JComponent panel, int keyCode, int modifiers, String actionName, Action act) {
        // 绑定组合键，例如 Ctrl + C
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        panel.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, actionName);

        // 设置对应的动作
        panel.getActionMap().put(actionName, act);
    }
}
