package com.xhf.leetcode.plugin.debug.utils;


import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.sun.jdi.Location;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugUtils {
    /**
     * 打印进程输出结果
     * @param process
     * @param asyn 是否异步
     */
    public static void printProcess(Process process, boolean asyn) {
        if (asyn) {
            new Thread(() -> {
                try {
                    LogUtils.simpleDebug("cmd result = " + IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LogUtils.error(e);
                }
            }).start();
            new Thread(() -> {
                try {
                    LogUtils.simpleDebug("cmd error result = " + IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LogUtils.error(e);
                }
            }).start();
        } else {
            try {
                LogUtils.simpleDebug("result = " + getOutputMessage(process));
                LogUtils.simpleDebug("error result = " + getErrorMessage(process));
            } catch (IOException e) {
                LogUtils.error(e);
            }
        }
    }
    public static String getErrorMessage(Process process) throws IOException {
        return inputStreamToString(process.getErrorStream(), "GBK");
    }

    public static String getOutputMessage(Process process) throws IOException {
        return inputStreamToString(process.getInputStream(), "GBK");
    }

    /**
     * 将 InputStream 转换为字符串，使用指定的编码
     * @param inputStream 输入流
     * @param encoding 字符编码
     * @return 转换后的字符串
     * @throws IOException 异常
     */
    public static String inputStreamToString(InputStream inputStream, String encoding) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 同时写入log和consoleView
     * @param message
     * @param project
     */
    public static void simpleDebug(String message, Project project) {
        simpleDebug(message, project, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void simpleDebug(String message, Project project, ConsoleViewContentType type) {
        // 强制换行
        if (message.charAt(message.length() - 1) != '\n') {
            message += "\n";
        }
        LogUtils.simpleDebug(message);
        ConsoleUtils.getInstance(project).simpleShowConsole(message, type);
    }

    /**
     * 移除当前文件显示的断点
     *
     * @param project project
     * @param addLine 断点行号
     */
    public static void removeCurrentVFileBreakpointAtLine(Project project, int addLine) {
        removeBreakpointAtLine(project, ViewUtils.getCurrentOpenVirtualFile(project), addLine);
    }

    /**
     * 移除当前文件的所有断点
     *
     * @param project project
     */
    public static void removeCurrentVFileAllBreakpoint(Project project) {
        removeAllBreakpoint(project, ViewUtils.getCurrentOpenVirtualFile(project));
    }

    /**
     * 根据文件路径和行号移除断点
     * @param project 当前项目
     * @param file 虚拟文件
     * @param lineNumber 行号（0-based）
     */
    public static void removeBreakpointAtLine(Project project, VirtualFile file, int lineNumber) {
        if (project == null || file == null) {
            return; // 防止空指针异常
        }

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();

        // 遍历所有断点，找到符合条件的断点
        for (XBreakpoint<?> breakpoint : getAllBreakpoint(project)) {
            if (breakpoint.getSourcePosition() != null
                    && file.equals(breakpoint.getSourcePosition().getFile())
                    && lineNumber == breakpoint.getSourcePosition().getLine()) {
                breakpointManager.removeBreakpoint(breakpoint);
                break;
            }
        }
    }

    /**
     * 根据文件路径移除所有断点
     * @param project 当前项目
     * @param file 虚拟文件
     */
    public static void removeAllBreakpoint(Project project, VirtualFile file) {
        if (project == null || file == null) {
            return; // 防止空指针异常
        }

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();

        List<XBreakpoint<?>> res = new ArrayList<>();
        // 遍历所有断点，找到符合条件的断点
        for (XBreakpoint<?> breakpoint : DebugUtils.getAllBreakpoint(project)) {
            if (breakpoint.getSourcePosition() != null
                    && file.equals(breakpoint.getSourcePosition().getFile())
                    ) {
                res.add(breakpoint);
            }
        }
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                for (XBreakpoint<?> re : res) {
                    breakpointManager.removeBreakpoint(re);
                }
            });
        });
    }

    /**
     * 根据当前打开文件，获取所有断点信息
     * 读操作需要在EDT线程中执行
     *
     * @param project project
     * @return 断点
     */
    public static List<XBreakpoint<?>> getAllBreakpoint(Project project) {
        // 确保在读操作中执行
        return ApplicationManager.getApplication().runReadAction(new Computable<>() {
            @Override
            public List<XBreakpoint<?>> compute() {
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                XBreakpoint<?>[] allBreakpoints = breakpointManager.getAllBreakpoints();
                return List.of(allBreakpoints); // 返回断点列表
            }
        });
    }

    // 将idea创建的断点转换为项目可以识别的指令
    // 很蠢, sp.getLine()从0开始计算的
    public static Instrument buildBInst(XSourcePosition sp) {
        return Instrument.success(ReadType.UI_IN, Operation.B, String.valueOf(sp.getLine() + 1));
    }

    public static Instrument buildRBInst(XSourcePosition sp) {
        return Instrument.success(ReadType.UI_IN, Operation.RB, String.valueOf(sp.getLine() + 1));
    }

    // 用于存储已添加的高亮
    private static final Map<Integer, RangeHighlighter> highlighterMap = new HashMap<>();

    public static void highlightLine(Project project, int lineNumber) {
        lineNumber = lineNumber - 1;
        FileEditor fileEditor = ViewUtils.getCurrentOpenEditor(project);
        TextEditor textEditor = (TextEditor) fileEditor;
        // 获取 Editor 对象
        assert textEditor != null;
        Editor editor = textEditor.getEditor();

        // 获取 MarkupModel
        MarkupModel markupModel = editor.getMarkupModel();
        // 移除别的高亮
        removeHighlightLine(project);

        // 获取 TextAttributes，用于设置文本高亮的颜色
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(new JBColor(new Color(61, 83, 206), new Color(61, 83, 206)));  // 设置背景颜色为蓝色
        textAttributes.setForegroundColor(JBColor.WHITE);

        simpleDebug("set highlight...", project);
        // 高亮当前行
        int startOffset = editor.getDocument().getLineStartOffset(lineNumber);
        int endOffset = editor.getDocument().getLineEndOffset(lineNumber);

        // 添加高亮
        int finalLineNumber = lineNumber;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                    startOffset,
                    endOffset,
                    Integer.MAX_VALUE, // 高亮的层级
                    textAttributes,
                    HighlighterTargetArea.LINES_IN_RANGE
            );
            // 缓存该高亮
            highlighterMap.put(finalLineNumber, highlighter);
        });
    }

    public static void removeHighlightLine(Project project) {
        FileEditor fileEditor = ViewUtils.getCurrentOpenEditor(project);
        TextEditor textEditor = (TextEditor) fileEditor;
        // 获取 Editor 对象
        assert textEditor != null;
        Editor editor = textEditor.getEditor();

        // 获取 MarkupModel
        MarkupModel markupModel = editor.getMarkupModel();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            for (RangeHighlighter highlighter : highlighterMap.values()) {
                markupModel.removeHighlighter(highlighter);
            }
            simpleDebug("removeHighlightLine done...", project);
        });
    }

    /**
     * 根据Location构建行信息
     * @param location location
     * @return
     */
    public static String buildCurrentLineInfoByLocation(Location location) {
        String className = location.declaringType().name(); // 类名
        String methodName = location.method().name(); // 方法名
        int lineNumber = location.lineNumber(); // 行号
        return className + "." + methodName + ":" + lineNumber;
    }

    public static void fillExecuteResultByLocation(ExecuteResult r, Location location) {
        r.setAddLine(location.lineNumber());
        r.setMethodName(location.method().name());
        r.setClassName(location.declaringType().name());
    }
}