package com.xhf.leetcode.plugin.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataKey;
import com.xhf.leetcode.plugin.window.LCPanel;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DataKeys {

    public static final DataKey<ConsoleView> LEETCODE_CONSOLE_VIEW = DataKey.create("LEETCODE_CONSOLE_VIEW");

    public static final DataKey<LCPanel.MyList> LEETCODE_QUESTION_LIST = DataKey.create("LEETCODE_QUESTION_LIST");

}
