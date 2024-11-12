package com.xhf.leetcode.plugin.io.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

@Service
public final class ConsoleUtils implements Disposable {

    private final Project project;
    private ConsoleView consoleView;

    public ConsoleUtils(Project project) {
        this.project = project;
    }

    public static ConsoleUtils getInstance(Project project) {
        return project.getService(ConsoleUtils.class);
    }

    public void showInfo(String content) {
        showConsole(() -> {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
            consoleView.print(content, ConsoleViewContentType.LOG_INFO_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
        });
    }

    public void showWaring(String content) {
        showConsole(() -> {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\n", ConsoleViewContentType.LOG_WARNING_OUTPUT);
            consoleView.print(content, ConsoleViewContentType.LOG_WARNING_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.LOG_WARNING_OUTPUT);
        });
    }

    private void showConsole(Runnable runnable) {
        // avoid concurrent question
        ApplicationManager.getApplication().invokeLater(() -> {
            if (consoleView == null) {
                this.consoleView = LCConsoleWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CONSOLE_VIEW);
            }
            runnable.run();
        });
    }

    @Override
    public void dispose() {

    }
}
