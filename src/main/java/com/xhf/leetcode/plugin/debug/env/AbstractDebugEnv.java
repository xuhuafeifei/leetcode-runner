package com.xhf.leetcode.plugin.debug.env;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import javax.swing.*;

import static javax.swing.JOptionPane.*;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractDebugEnv implements DebugEnv {
    /**
     * 核心存储路径. 所有debug相关文件都存储在filePath指定目录下
     */
    protected String filePath = "E:\\java_code\\lc-test\\cache\\debug";

    protected final Project project;
    /**
     * Solution核心方法名
     */
    protected String methodName;

    protected static boolean isDebug = false;

    public AbstractDebugEnv(Project project) {
        this.project = project;
        this.filePath = new FileUtils.PathBuilder(AppSettings.getInstance().getCoreFilePath()).append("debug").build();
        initFilePath();
    }

    /**
     * 允许子类覆盖filePath路径
     */
    protected abstract void initFilePath();


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
        if (i != OK_OPTION) {
            return false;
        }

        String testcase = jTextPane.getText();
        lc.setDebugTestcase(testcase);
        // update
        ViewUtils.updateLeetcodeEditorByCurrentVFile(project, lc);
        return true;
    }

    @Override
    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public void stopDebug() {
        isDebug = false; // 先置为false, 否则后续判断debug状态可能会出现问题. 在结束时可能会出现并发问题. DebugStopAction一个Thread, BQ消费一个Thread
        InstSource.clear();
        DebugUtils.removeHighlightLine(project);
        DebugUtils.simpleDebug("debug env stop!", project);
        LCEventBus.getInstance().post(new DebugEndEvent());
    }

    public void startDebug() {
        isDebug = true;
        InstSource.clear();
        DebugUtils.simpleDebug("debug env start", project);

        LCEventBus.getInstance().post(new DebugStartEvent());
    }

    // 拷贝py文件. 可以选择不实现, 毕竟有可能没有copy的需求
    protected boolean copyFile() {
        return true;
    }

    protected boolean copyFileHelper(String resourcePath) {
        String[] split = resourcePath.split("/");
        String fileName = split[split.length - 1];
        return StoreService.getInstance(project).
                copyFile(getClass().getResource(resourcePath),
                        new FileUtils.PathBuilder(filePath).append(fileName).build()
                );
    }

    protected abstract boolean buildToolPrepare() throws DebugError;

    protected abstract boolean createMainFile() throws DebugError;

    protected abstract boolean createSolutionFile() throws DebugError;
}
