package com.xhf.leetcode.plugin.window;

import com.google.common.eventbus.Subscribe;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.xhf.leetcode.plugin.bus.DebugEndEvent;
import com.xhf.leetcode.plugin.bus.DebugStartEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.Constants;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.HotKeyUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@LCSubscriber(events = {DebugStartEvent.class, DebugEndEvent.class})
public class LCConsolePanel extends SimpleToolWindowPanel implements DataProvider, Disposable {

    private final ConsoleView consoleView;
    private final JBSplitter jbSplitter;
    private final Project project;
    /**
     * 命令行输入框, 用于接收用户输入的命令行数据
     */
    private final JTextField inputField;
    /**
     * 命令行选项卡, 用于呈现Console 和 Variables 选项卡
     */
    private final JBEditorTabs tabs;
    /**
     * console选项卡, 显示console信息
     */
    private final TabInfo consoleTab;
    /**
     * variables选项卡, 显示variables信息. 其中包含stdPanel和variablePanel
     */
    private final TabInfo variablesTab;
    /**
     * 标准输出/错误显示面板, 用于呈现debug过程中, 代码的标准输出和标准错误
     */
    private final StdPanel stdPanel;
    /**
     * 在debug过程中, 存储变量的面板, 同时提供表达式计算的输入功能
     */
    private final VariablePanel variablePanel;
    /**
     * 命令行历史记录, 用于实现命令行历史记录功能
     */
    private HistoryCommand historyCommand;

    public LCConsolePanel(ToolWindow toolWindow, Project project) {
        super(Boolean.FALSE, Boolean.TRUE);
        this.project = project;
        LogUtils.simpleDebug("create LCConsolePanel");

        this.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        LogUtils.simpleDebug("consoleView created");

        // Register the consoleView for disposal when the panel is disposed
        // 解决console关闭时的内存泄露问题
        Disposer.register(this, consoleView);

        // 创建选项卡 (基于 JBEditorTabs)
        tabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);
        tabs.setBorder(Constants.BORDER);

        // 添加 Variables 选项卡
        stdPanel = new StdPanel();
        variablePanel = new VariablePanel();
        variablePanel.setAlignmentX(0);
        variablePanel.setAlignmentY(0);
        JBSplitter varAndExpSplitter = new JBSplitter();
        varAndExpSplitter.setFirstComponent(variablePanel);
        varAndExpSplitter.setSecondComponent(stdPanel);

        this.variablesTab = new TabInfo(varAndExpSplitter);
        variablesTab.setText(BundleUtils.i18n("action.leetcode.console.variablesTab"));
        variablesTab.setIcon(AllIcons.Debugger.Threads);
        tabs.addTab(variablesTab);

        // 添加 Console 选项卡
        this.consoleTab = new TabInfo(consoleView.getComponent());
        this.consoleTab.setText(BundleUtils.i18n("action.leetcode.console.consoleTab"));
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
        JLabel label = new JLabel();
        label.setText(BundleUtils.i18n("action.leetcode.console.commandLabel"));

        commandPanel.add(label, BorderLayout.NORTH);
        commandPanel.add(inputField, BorderLayout.CENTER);
        // 绑定热键
        AbstractAction up = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputField.setText(historyCommand.moveUp());
            }
        };
        HotKeyUtils.bindKey(inputField, KeyEvent.VK_UP, "moveUp", up);  // 上移
        HotKeyUtils.bindKey(inputField, KeyEvent.VK_KP_UP, "moveUp", up);  // 上移

        AbstractAction down = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputField.setText(historyCommand.moveDown());
            }
        };
        HotKeyUtils.bindKey(inputField, KeyEvent.VK_DOWN, "moveDown", down);  // 下移
        HotKeyUtils.bindKey(inputField, KeyEvent.VK_KP_DOWN, "moveDown", down);  // 下移

        // 使用 JSplitPane 创建分割面板
        jbSplitter = new JBSplitter();
        jbSplitter.setFirstComponent(tabs);
        jbSplitter.setSecondComponent(commandPanel);
        jbSplitter.setProportion(0.8f);
        jbSplitter.getSecondComponent().setVisible(false);

        LCEventBus.getInstance().register(this);

        // 添加工具栏(具有debug的功能)
        DefaultActionGroup ag = (DefaultActionGroup) ActionManager.getInstance()
            .getAction("leetcode.plugin.consoleToolbar");
        ActionToolbar toolbar = ActionManager.getInstance()
            .createActionToolbar("LCConsoleToolbar", ag, true);
        // 设置工具栏的排列方式为竖向排列
        toolbar.setOrientation(SwingConstants.VERTICAL);  // 设置竖直排列
        // 设置目标组件
        toolbar.setTargetComponent(jbSplitter);
        // 添加到父容器
        add(toolbar.getComponent(), BorderLayout.WEST);

        // 添加默认工具栏(具有清空, lineWrap等功能)
        DefaultActionGroup consoleGroup = new DefaultActionGroup(consoleView.createConsoleActions());
        ActionToolbar consoleToolbar = ActionManager.getInstance()
            .createActionToolbar("MyLCConsoleToolbar", consoleGroup, false);
        consoleToolbar.setOrientation(SwingConstants.VERTICAL);  // 设置竖直排列
        consoleToolbar.setTargetComponent(jbSplitter);
        add(consoleToolbar.getComponent(), BorderLayout.EAST);

        setToolbar(toolbar.getComponent());

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
        if (!flag) {
            ConsoleUtils.getInstance(project).showError(
                BundleUtils.i18n("error.command.write"), false
            );
        }
        // 清空输入框
        inputField.setText("");
        if (historyCommand != null) {
            historyCommand.add(debugCommand);
        }
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.LEETCODE_CONSOLE_VIEW.is(dataId)) {
            return consoleView;
        } else if (DataKeys.LEETCODE_DEBUG_VARIABLE_LIST.is(dataId)) {
            return variablePanel.getVariables();
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
        variablePanel.getVariables().setNonData();

        AppSettings instance = AppSettings.getInstance();
        ApplicationManager.getApplication().invokeLater(() -> {
            if (instance.isCommandReader()) {
                openSecond();
                // 初始化命令行记录器
                historyCommand = new HistoryCommand();
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
        MyList<String> variableList = variablePanel.getVariables();
        variableList.setNonData();
        variableList.setEmptyText("debug结束...");
        // gc
        if (historyCommand != null) {
            historyCommand = null;
        }
    }

    /**
     * 命令行历史记录
     */
    static class HistoryCommand {

        History dummyHead = new History();
        History point = dummyHead;

        /**
         * 新添加的记录永远是最后一个
         *
         * @param cmd cmd
         */
        public void add(String cmd) {
            point.next = new History(cmd, point, null);
            point = point.next;
        }

        public String moveUp() {
            if (point.pre == dummyHead) {
                return point.cmd;
            }
            if (point == dummyHead) {
                return point.cmd;
            }
            String res = point.cmd;
            point = point.pre;
            return res;
        }

        public String moveDown() {
            if (point.next == null) {
                return point.cmd;
            }
            String res = point.next.cmd;
            point = point.next;
            return res;
        }

        static class History {

            String cmd;
            History pre;
            History next;

            public History(String cmd) {
                this.cmd = cmd;
            }

            public History(String cmd, History pre, History next) {
                this.cmd = cmd;
                this.pre = pre;
                this.next = next;
            }

            public History() {
            }
        }
    }
}
