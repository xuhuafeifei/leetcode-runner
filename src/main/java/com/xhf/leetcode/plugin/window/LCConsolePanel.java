package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.util.ui.JBUI;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.render.VariablesCellRender;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.Constants;
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
    private final JTextField inputField;
    private final JBEditorTabs tabs;
    /**
     * 存储变量
     */
    private final MyList<String> variableList = new MyList<>();
    private final TabInfo consoleTab;
    private final TabInfo variablesTab;
    private final StdPanel stdPanel;

    public LCConsolePanel(ToolWindow toolWindow, Project project) {
        super(Boolean.FALSE, Boolean.TRUE);
        this.project = project;
        LogUtils.simpleDebug("create LCConsolePanel");

        this.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        LogUtils.simpleDebug("consoleView created");

        // Register the consoleView for disposal when the panel is disposed
        // 解决console关闭时的内存泄露问题
        Disposer.register(this, consoleView);

        variableList.setCellRenderer(new VariablesCellRender());

        // 创建选项卡 (基于 JBEditorTabs)
        tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);
        tabs.setBorder(Constants.BORDER);

        // 添加 Local Variables 选项卡
        stdPanel = new StdPanel();
        JBSplitter varAndExpSplitter = new JBSplitter();
        varAndExpSplitter.setFirstComponent(new JBScrollPane(variableList));
        varAndExpSplitter.setSecondComponent(stdPanel);

        this.variablesTab = new TabInfo(varAndExpSplitter);
        variablesTab.setText("Variables & Expressions");
        variablesTab.setIcon(AllIcons.Debugger.Threads);
        tabs.addTab(variablesTab);

        // 添加 Console 选项卡
        this.consoleTab = new TabInfo(consoleView.getComponent());
        consoleTab.setText("Console");
        consoleTab.setIcon(AllIcons.Debugger.Console);
        tabs.addTab(consoleTab);

        tabs.select(consoleTab, true);

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
        jbSplitter.setFirstComponent(tabs);
        jbSplitter.setSecondComponent(commandPanel);
        jbSplitter.setProportion(0.8f);
        jbSplitter.getSecondComponent().setVisible(false);

        LCEventBus.getInstance().register(this);

        // 添加默认工具栏(具有清空, lineWrap等功能)
        DefaultActionGroup consoleGroup = new DefaultActionGroup(consoleView.createConsoleActions());
        ActionToolbar consoleToolbar = ActionManager.getInstance().createActionToolbar("MyLCConsoleToolbar", consoleGroup, true);
        consoleToolbar.setTargetComponent(jbSplitter);
        setToolbar(consoleToolbar.getComponent());

        // 添加工具栏(具有debug的功能)
        DefaultActionGroup ag = (DefaultActionGroup) ActionManager.getInstance().getAction("leetcode.plugin.consoleToolbar");
        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("LCConsoleToolbar", ag, true);

        // 设置工具栏的排列方式为竖向排列
        toolbar.setOrientation(SwingConstants.VERTICAL);  // 设置竖直排列

        // 设置目标组件
        toolbar.setTargetComponent(jbSplitter);

        // 添加到父容器
        add(toolbar.getComponent(), BorderLayout.WEST);

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
        // 存储用户输入
        boolean flag = InstSource.userCmdInput(debugCommand);
        if (! flag) {
            ConsoleUtils.getInstance(project).showError("command write failed!", false);
        }
        // 清空输入框
        inputField.setText("");
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_CONSOLE_VIEW.is(dataId)) {
            return consoleView;
        } else if (DataKeys.LEETCODE_DEBUG_VARIABLE_LIST.is(dataId)) {
            return variableList;
        } else if (DataKeys.LEETCODE_DEBUG_STDPANEL.is(dataId)) {
            return stdPanel;
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
        variableList.setNonData();

        AppSettings instance = AppSettings.getInstance();
        ApplicationManager.getApplication().invokeLater(() -> {
            if (instance.isCommandReader()) {
                openSecond();
            }
            if (instance.isUIOutput()) {
                tabs.select(variablesTab, true);
            }
            if (instance.isConsoleOutput()) {
                tabs.select(consoleTab, true);
            }
            stdPanel.clear();
        });
    }

    @Subscribe
    public void DebugEndEventListener(DebugEndEvent event) {
        closeSecond();
        variableList.setNonData();
        variableList.setEmptyText("Debug finish...");
    }
}
