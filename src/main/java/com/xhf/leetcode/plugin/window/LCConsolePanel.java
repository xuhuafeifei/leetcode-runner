package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LCSubscriber(events = {DebugStartEvent.class, DebugEndEvent.class})
public class LCConsolePanel extends SimpleToolWindowPanel implements DataProvider, Disposable {

    private final ConsoleView consoleView;
    private final JBSplitter jbSplitter;
    private final Project project;
    private JTextField inputField;
    private final JPanel consolePanel;

    public LCConsolePanel(ToolWindow toolWindow, Project project) {
        super(Boolean.FALSE, Boolean.TRUE);
        this.project = project;
        LogUtils.simpleDebug("create LCConsolePanel");

        this.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        LogUtils.simpleDebug("consoleView created");

        LogUtils.simpleDebug("Content set");

        // Register the consoleView for disposal when the panel is disposed
        // 解决console关闭时的内存泄露问题
        Disposer.register(this, consoleView);

        // 设置 ConsoleView 组件
        this.consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleView.getComponent(), BorderLayout.CENTER);

        // 创建输入框
        this.inputField = new JTextField();
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 处理用户输入
                handleInput(inputField.getText());
            }
        });

        // 创建command输入框的面板
        JPanel commandPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Debug Command Line");

        commandPanel.add(label, BorderLayout.NORTH);
        commandPanel.add(inputField, BorderLayout.CENTER);

        // 使用 JSplitPane 创建分割面板
        jbSplitter = new JBSplitter();
        jbSplitter.setFirstComponent(consolePanel);
        jbSplitter.setSecondComponent(commandPanel);
        jbSplitter.setProportion(0.8f);
        jbSplitter.getSecondComponent().setVisible(false);

        LCEventBus.getInstance().register(this);

        // 添加分割面板到 LCConsolePanel
        add(jbSplitter, BorderLayout.CENTER);
    }

    private void closeSecond() {
        jbSplitter.getSecondComponent().setVisible(false);
    }

    private void openSecond() {
        jbSplitter.getSecondComponent().setVisible(true);
    }

    // 处理输入内容
    private void handleInput(String debugCommand) {
        // 将用户输入的内容输出到 ConsoleView
        ConsoleUtils.getInstance(project).userCmdInput(debugCommand);

        // 清空输入框
        inputField.setText("");
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_CONSOLE_VIEW.is(dataId)) {
            return consoleView;
        }
        return super.getData(dataId);
    }

    public void dispose() {
        if (consoleView != null) {
            Disposer.dispose(consoleView);
        }
    }

    @Subscribe
    public void DebugStartEventListener(DebugStartEvent event) {
        LogUtils.simpleDebug("open command line...");

        // debug
        LogUtils.simpleDebug(Thread.currentThread().toString());

        openSecond();
    }

    @Subscribe
    public void DebugEndEventListener(DebugEndEvent event) {
        closeSecond();
    }
}
