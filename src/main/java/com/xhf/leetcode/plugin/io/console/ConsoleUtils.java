package com.xhf.leetcode.plugin.io.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.xhf.leetcode.plugin.io.console.utils.ConsoleDialog;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.Safe;
import com.xhf.leetcode.plugin.utils.UnSafe;
import com.xhf.leetcode.plugin.window.LCConsolePanel;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
@Service
public final class ConsoleUtils implements Disposable {

    private static final String LEETCODE_CODE_DIALOG_TITLE = "plugin info";
    private final Project project;
    private ConsoleView consoleView;

    public ConsoleUtils(Project project) {
        this.project = project;
    }

    public static ConsoleUtils getInstance(Project project) {
        return project.getService(ConsoleUtils.class);
    }


    @UnSafe("该方法在多线程的情况下并不安全. 因为他在底层可能会弹出对话框. 而该操作会凝固线程, 导致EDT阻塞." +
            "所以在多线程的情况下, 尽可能不要使用该方法. 在本项目中, 设计多线程情况多是触发Event")
    public void showInfo(String content, boolean clear, boolean showDialog, String message, String dialogTitle, ConsoleDialog consoleDialog) {
        showConsole(() -> {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
            consoleView.print(content, ConsoleViewContentType.LOG_INFO_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
        }, clear, showDialog, message, dialogTitle, consoleDialog);
    }

    /**
     * 显示info信息
     * @param content
     */

    public void showInfo(String content) {
        showInfo(content, false, false, null, null, ConsoleDialog.UNKNOWN);
    }

    /**
     * 显示info信息, 但不弹出对话框
     * @param content
     * @param clear
     */
    @Safe
    public void showInfo(String content, boolean clear) {
        showInfo(content, clear, false, null, null, ConsoleDialog.UNKNOWN);
    }


    @UnSafe("该方法在多线程的情况下并不安全. 因为他在底层可能会弹出对话框. 而该操作会凝固线程, 导致EDT阻塞." +
            "所以在多线程的情况下, 尽可能不要使用该方法. 在本项目中, 设计多线程情况多是触发Event")
    public void showWaring(String content, boolean clear, boolean showDialog, String message, String dialogTitle, ConsoleDialog consoleDialog) {
        showConsole(() -> {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\n", ConsoleViewContentType.LOG_WARNING_OUTPUT);
            consoleView.print(content, ConsoleViewContentType.LOG_WARNING_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.LOG_WARNING_OUTPUT);
        },  clear, showDialog, message, dialogTitle, consoleDialog);
    }

    @Safe
    public void showWaring(String content) {
        showWaring(content, false, false, null, null, ConsoleDialog.UNKNOWN);
    }

    /**
     * 显示warning信息, 但不弹出对话框
     * @param content
     * @param clear
     */
    @Safe
    public void showWaring(String content, boolean clear) {
        showWaring(content, clear, false, null, null, ConsoleDialog.UNKNOWN);
    }

    private void showConsole(Runnable runnable, boolean clear, boolean showDialog, String message, String dialogTitle, ConsoleDialog consoleDialog) {
        // avoid concurrent question
        ApplicationManager.getApplication().invokeLater(() -> {
            if (consoleView == null) {
                try {
                    this.consoleView = LCConsoleWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CONSOLE_VIEW);
                } catch (Exception e) {
                    LogUtils.error("consoleView load error", e);
                    return;
                }
            }
            assert consoleView != null;
            // 清空
            if (clear) {
                consoleView.clear();
            }
            // 弹出
            // 获取并显示 ToolWindow（确保控制台窗口可见）
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = toolWindowManager.getToolWindow(LCConsoleWindowFactory.ID);
            if (toolWindow != null && !toolWindow.isVisible()) {
                toolWindow.show();  // 显示控制台窗口
            }
            consoleView.getComponent().setVisible(true);

            runnable.run();

            // 显示弹窗
            showDialog(showDialog, message, dialogTitle, consoleDialog);
        });
    }

    /**
     * 显示弹窗
     * @param showDialog 是否显示弹窗
     * @param message 显示信息
     * @param dialogTitle dialog的title
     * @param consoleDialog 弹窗类型
     */
    private void showDialog(boolean showDialog, String message, String dialogTitle, ConsoleDialog consoleDialog) {
        if (showDialog && StringUtils.isNotBlank(message)) {
            String title;
            if (StringUtils.isBlank(dialogTitle)) {
                title = LEETCODE_CODE_DIALOG_TITLE;
            } else {
                title = dialogTitle;
            }
            switch (consoleDialog) {
                case INFO:
                    Messages.showInfoMessage(project, message, title);
                    break;
                case WARNING:
                    Messages.showWarningDialog(project, message, title);
                    break;
                case ERROR:
                    Messages.showErrorDialog(project, message, title);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        if (consoleView != null) {
            consoleView.clear();
            consoleView.dispose();
        }
    }
}
