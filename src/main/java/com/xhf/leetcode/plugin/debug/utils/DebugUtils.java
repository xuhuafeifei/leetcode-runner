package com.xhf.leetcode.plugin.debug.utils;


import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
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
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Value;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbElement;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LangType;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DebugUtils {
    /**
     * 打印进程输出结果
     * @param process process
     * @param asyn 是否异步
     */
    public static void printProcess(Process process, boolean asyn, Project project) {
        if (asyn) {
            new Thread(() -> {
                try {
                    String outputMessage = getOutputMessage(process);
                    LogUtils.simpleDebug("cmd result = " + outputMessage);
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            }).start();
            new Thread(() -> {
                try {
                    String errorMessage = getErrorMessage(process);
                    if (StringUtils.isBlank(errorMessage)) {
                        return;
                    }
                    LogUtils.simpleDebug("cmd error result = " + errorMessage);
                    if (project != null) {
                        ConsoleUtils.getInstance(project).showError(errorMessage, false);
                    } else {
                        LogUtils.simpleDebug("Project为NULL, 当前为测试模式");
                    }
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            }).start();
        } else {
            try {
                LogUtils.simpleDebug("result = " + getOutputMessage(process));
                String errorMessage = getErrorMessage(process);
                LogUtils.simpleDebug("error result = " + errorMessage);
                if (project != null) {
                    ConsoleUtils.getInstance(project).showError(errorMessage, false);
                } else {
                    LogUtils.simpleDebug("Project为NULL, 当前为测试模式");
                }
            } catch (Exception e) {
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
     * @param message message
     * @param project project
     */
    public static void simpleDebug(String message, Project project) {
        simpleDebug(message, project, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * 同时写入log和consoleView
     *
     * @param message message
     * @param project project
     * @param isShow 是否弹出console
     */
    public static void simpleDebug(String message, Project project, boolean isShow) {
        simpleDebug(message, project, ConsoleViewContentType.NORMAL_OUTPUT, isShow);
    }

    /**
     * 同时写入log和consoleView
     *
     * @param message message
     * @param project project
     * @param type 控制台打印信息颜色
     * @param isShow 是否弹出console
     */
    public static void simpleDebug(String message, Project project, ConsoleViewContentType type, boolean isShow) {
        // 强制换行
        if (message.charAt(message.length() - 1) != '\n') {
            message += "\n";
        }
        LogUtils.simpleDebug(message);
        ConsoleUtils instance = ConsoleUtils.getInstance(project);
        if (instance == null) {
            LogUtils.simpleDebug("当前为测试模式, 不启动console output");
            return;
        }
        instance.simpleShowConsole(message, type, isShow);
    }

    /**
     * 默认弹出console
     *
     * @param message message
     * @param project project
     * @param type 控制台打印信息的颜色
     */
    public static void simpleDebug(String message, Project project, ConsoleViewContentType type) {
        simpleDebug(message, project, type, true);
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
                ApplicationManager.getApplication().invokeLater(() -> {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        // 删除断点
                        breakpointManager.removeBreakpoint(breakpoint);
                    });
                });
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
    public static Instruction buildBInst(XSourcePosition sp) {
        return Instruction.success(ReadType.UI_IN, Operation.B, String.valueOf(sp.getLine() + 1));
    }

    public static Instruction buildRBInst(XSourcePosition sp) {
        return Instruction.success(ReadType.UI_IN, Operation.RB, String.valueOf(sp.getLine() + 1));
    }

    // 用于存储已添加的高亮
    private static final Map<Integer, RangeHighlighter> highlighterMap = new HashMap<>();

    public static void highlightLineWithCheck(int lineNumber, String curClassName, Project project) {
        // 如果是cpp debug, 特判一下
        String langType = AppSettings.getInstance().getLangType();
        if (LangType.CPP.getLangType().equals(langType)) {
            if (! curClassName.endsWith("solution.cpp")) {
                DebugUtils.removeHighlightLine(project);
                return;
            }
        }
        // 如果当前执行的类不是Solution, 则不进行高亮
        else if (! "Solution".equals(curClassName)) {
            DebugUtils.removeHighlightLine(project);
            return;
        }
        DebugUtils.simpleDebug("当前执行行号: " + lineNumber + " 设置高亮...", project);
        DebugUtils.highlightLine(project, lineNumber);
    }

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

            // 滚动到指定行
            ScrollingModel scrollingModel = editor.getScrollingModel();
            scrollingModel.scrollTo(new LogicalPosition(finalLineNumber, 0), ScrollType.CENTER);
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
        ApplicationManager.getApplication().invokeLater(() -> {
            for (RangeHighlighter highlighter : highlighterMap.values()) {
                markupModel.removeHighlighter(highlighter);
            }
            simpleDebug("移除文件高亮代码...", project);
        });
    }

    /**
     * 根据Location构建行信息
     * @param location location
     * @return 行信息
     */
    public static String buildCurrentLineInfoByLocation(Location location) {
        String className = location.declaringType().name(); // 类名
        String methodName = location.method().name(); // 方法名
        int lineNumber = location.lineNumber(); // 行号
        return buildCurrentLineInfo(className, methodName, lineNumber);
    }

    public static String buildCurrentLineInfo(String className, String methodName, int lineNumber) {
        return className + "." + methodName + ":" + lineNumber;
    }

    public static void fillExecuteResultByLocation(ExecuteResult r, Location location) {
        if (location == null) return;
        try {
            r.setAddLine(location.lineNumber());
            r.setMethodName(location.method().name());
            r.setClassName(location.declaringType().name());
        } catch (Exception e) {
            DebugUtils.simpleDebug("fillExecuteResultByLocation error: " + e.getMessage(), r.getContext().getProject());
        }
    }

    public static void fillExecuteResultByLocation(ExecuteResult r, String className, String methodName, int lineNumber) {
        r.setAddLine(lineNumber);
        r.setMethodName(methodName);
        r.setClassName(className);
    }

    public static void fillExecuteResultByGdbElement(ExecuteResult r, GdbElement className, GdbElement methodName, GdbElement lineNumber) {
        r.setAddLine(lineNumber.getAsGdbPrimitive().getAsNumber().intValue());
        r.setMethodName(methodName.getAsGdbPrimitive().toString());
        r.setClassName(className.getAsGdbPrimitive().getAsString());
    }

    public static String buildCurrentLineInfoByLocation(ExecuteResult r) {
        return buildCurrentLineInfo(r.getClassName(), r.getMethodName(), r.getAddLine());
    }

    /**
     * 获取可用端口
     * @return port
     */
    public static int findAvailablePort() {
        // socket使用完后立刻关闭
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new DebugError("Failed to find an available port", e);
        }
    }

    /**
     * 服务于变量转换
     * @param values values
     * @return  map
     */
    public static Map<String, Value> convert(Map<LocalVariable, Value> values) {
        Map<String, Value> res = new HashMap<>();
        for (Map.Entry<LocalVariable, Value> entry : values.entrySet()) {
            res.put(entry.getKey().name(), entry.getValue());
        }
        return res;
    }

    /**
     * 哎，不想命名了, 就这吧
     * @param  values values
     * @return map
     */
    public static Map<String, Value> convert2(Map<Field, Value> values) {
        Map<String, Value> res = new HashMap<>();
        for (Map.Entry<Field, Value> entry : values.entrySet()) {
            res.put(entry.getKey().name(), entry.getValue());
        }
        return res;
    }
    public static String removeQuotes(String str) {
        // 除去的是字符串两端的双引号
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }
        // return str.replaceAll("^\"|\"$", "");
        return str;
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 匹配与更换信息
     * 该方法匹配形如 'breakpoint at line 数字' 的内容. 并且在'数字'累加offset
     * 请注意, 该方法只能匹配单行数据, 也就是说input不能包含换行符, 且必须是 line 数字 这样的形式
     *
     * @param input input
     * @param offset offset
     * @return String
     */
    public static String matchLine(String input, int offset) {
        // 正则匹配数字的模式
        Pattern pattern = Pattern.compile("[l|L]ine\\s*(\\d+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String lineNumber = matcher.group(1);
            int number = Integer.parseInt(lineNumber) + offset;
            input = input.replace(lineNumber, String.valueOf(number));
        }
        // 追加最后一段未匹配的内容
        return input;
    }

    public static String matchLines(String input, int offset) {
        String[] split = input.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(matchLine(s, offset)).append("\n");
        }
        return sb.toString();
    }

    public static boolean isPortAvailable(String host, int port) {
        try (Socket ignored = new Socket(host, port)) {
            // 如果能够成功建立连接，说明端口已经启动
            return true;
        } catch (IOException e) {
            // 如果抛出异常，说明端口未启动或不可访问
            return false;
        }
    }

    /**
     * 换了一种判断方式, 如果Java创建ServerSocket失败, 认为端口可用
     * C++的debug场景下, C++服务只支持HTTP, 不支持Socket, 所以这里不采用链接的方式判断
     * @param host host
     * @param port port
     * @return boolean
     */
    public static boolean isPortAvailable2(String host, int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 如果exp没有双引号, 增加双引号, 否则不增加
     * @param exp exp
     * @return string
     */
    public static String addQuotes(String exp) {
        String trim = exp.trim();
        String s = removeQuotes(trim);
        return "\"" + s + "\"";
    }
}
