package com.xhf.leetcode.plugin.utils;

import com.google.common.base.CharMatcher;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataKey;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.window.LCPanel;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DataKeys {

    public static final DataKey<ConsoleView> LEETCODE_CONSOLE_VIEW = DataKey.create("LEETCODE_CONSOLE_VIEW");

    public static final DataKey<MyList<Question>> LEETCODE_QUESTION_LIST = DataKey.create("LEETCODE_QUESTION_LIST");

    public static final DataKey<MyList<String>> LEETCODE_DEBUG_VARIABLE_LIST = DataKey.create("LEETCODE_DEBUG_VARIABLE_LIST ");
}
