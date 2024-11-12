package com.xhf.leetcode.plugin.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataKey;
import com.xhf.leetcode.plugin.window.LCPanel;

public class DataKeys {

    public static final DataKey<ConsoleView> LEETCODE_CONSOLE_VIEW = DataKey.create("LEETCODE_CONSOLE_VIEW");

//    public static final DataKey<MyTable> LEETCODE_QUESTION_TABLE = DataKey.create("LEETCODE_QUESTION_TABLE ");
//    public static final DataKey<QSetPanel> LEETCODE_QUESTION_SET_PANEL = DataKey.create("LEETCODE_QUESTION_SET_PANEL");
    public static final DataKey<LCPanel.MyList> LEETCODE_QUESTION_LIST = DataKey.create("LEETCODE_QUESTION_LIST");

}
