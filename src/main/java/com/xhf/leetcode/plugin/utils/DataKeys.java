package com.xhf.leetcode.plugin.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.DataKey;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.window.StdPanel;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DataKeys {

    public static final DataKey<ConsoleView> LEETCODE_CONSOLE_VIEW = DataKey.create("LEETCODE_CONSOLE_VIEW");

    public static final DataKey<MyList<Question>> LEETCODE_QUESTION_LIST = DataKey.create("LEETCODE_QUESTION_LIST");

    public static final DataKey<MyList<String>> LEETCODE_DEBUG_VARIABLE_LIST = DataKey.create("LEETCODE_DEBUG_VARIABLE_LIST");

    public static final DataKey<StdPanel> LEETCODE_DEBUG_STDPANEL = DataKey.create("LEETCODE_DEBUG_STDPANEL");
    /**
     * 获取hot 100 panel当前显示的数据
     */
    public static final DataKey<MyList<Question>> LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST = DataKey.create("LEETCODE_DEEP_CODING_HOT_100_QUESTION_LIST");
    /**
     * 获取interview 150 panel当前显示的数据
     */
    public static final DataKey<MyList<Question>> LEETCODE_DEEP_CODING_INTERVIEW_100_QUESTION_LIST = DataKey.create("LEETCODE_DEEP_CODING_INTERVIEW_100_QUESTION_LIST ");
    /**
     * 当前状态, 是正常模式还是deep coding模式
     */
    public static final DataKey<Boolean> LEETCODE_CODING_STATE = DataKey.create("LEETCODE_CODING_STATE");
    /**
     * deep coding模块当前选中的tab名称
     */
    public static final DataKey<String> LEETCODE_CHOOSEN_TAB_NAME = DataKey.create("LEETCODE_CHOOSEN_TAB_NAME ");
}
