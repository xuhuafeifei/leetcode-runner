package com.xhf.leetcode.plugin.debug.output;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;
import com.xhf.leetcode.plugin.window.StdPanel;

/**
 * debug调试代码的Std out, Std error 的输出管理类
 * <p>
 * 该类非常特殊, 他只负责处理debug 代码的std信息, 不负责处理其他内容. 因此该类
 * 无法集成到OutputType中.
 * <p>
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class OutputHelper {

    private final Project project;
    private final StdPanel stdPanel;

    public static final String STD_OUT = "STD_OUT";
    public static final String STD_ERROR = "STD_ERROR";

    public OutputHelper(Project project) {
        this.project = project;
        this.stdPanel = LCConsoleWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEBUG_STDPANEL);
    }
    public void output(ExecuteResult r) {
        AppSettings setting = AppSettings.getInstance();
        switch (r.getMoreInfo()) {
            case STD_OUT:
                if (setting.isUIOutput()) {
                    stdPanel.appendStdoutContent(r.getResult());
                } else if (setting.isConsoleOutput()) {
                    ConsoleUtils.getInstance(project).simpleShowConsole(r.getResult(),  ConsoleViewContentType.LOG_WARNING_OUTPUT, true);
                }
                break;
            case STD_ERROR:
                if (setting.isUIOutput()) {
                    stdPanel.appendStderrContent(r.getResult());
                } else if (setting.isConsoleOutput()) {
                    ConsoleUtils.getInstance(project).simpleShowConsole(r.getResult(),  ConsoleViewContentType.ERROR_OUTPUT, true);
                }
                break;
            default:
                DebugUtils.simpleDebug(r.getMsg(), project, ConsoleViewContentType.ERROR_OUTPUT, true);
        }
    }

    /**
     * 主要是清除UI上一轮std输出
     */
    public void clear() {
        stdPanel.clear();
    }
}
