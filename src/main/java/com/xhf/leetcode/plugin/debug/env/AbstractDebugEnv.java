package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;

import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractDebugEnv implements DebugEnv {
    protected final Project project;
    /**
     * Solution核心方法名
     */
    protected String methodName;

    protected static boolean isDebug = false;

    public AbstractDebugEnv(Project project) {
        this.project = project;
    }

    @Override
    public abstract boolean prepare() throws DebugError;

    protected boolean testcasePrepare() {
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByCurrentVFile(project);
        JTextArea jTextPane = new JTextArea(10, 40);
        jTextPane.setLineWrap(false);
        // 获取之前存储的debug testcase
        jTextPane.setText(lc.getDebugTestcase());

        int i = JOptionPane.showOptionDialog(
                null,
                new JBScrollPane(jTextPane),
                "设置debug的输入(只能设置一个测试案例)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"确定", "取消"},
                "确定"
        );
        if (i == CANCEL_OPTION || i == NO_OPTION) {
            return false;
        }

        String testcase = jTextPane.getText();
        lc.setDebugTestcase(testcase);
        // update
        ViewUtils.updateLeetcodeEditorByCurrentVFile(project, lc);
        return true;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public void stopDebug() {
        isDebug = false;
        InstSource.clear();
        DebugUtils.simpleDebug("debug service stop", project);
        LCEventBus.getInstance().post(new DebugEndEvent());
    }

    public void startDebug() {
        isDebug = true;
        InstSource.clear();
        // 清空consoleView
        ConsoleUtils.getInstance(project).clearConsole();
        DebugUtils.simpleDebug("debug service starting...", project);
        // debug
        LogUtils.simpleDebug(Thread.currentThread().toString());

        LCEventBus.getInstance().post(new DebugStartEvent());
    }
}
