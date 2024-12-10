package com.xhf.leetcode.plugin.utils;

import com.intellij.ide.script.IDE;
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LogUtils {
    private static final Logger IDEA_LOGGER = Logger.getInstance(LogUtils.class);
    // private static final Log LOG = LogFactory.getLog(LogUtils.class);

    static {
        IDEA_LOGGER.setLevel(LogLevel.INFO);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String formatLogMessage(String level, String message) {
        // 获取当前时间
        String timestamp = DATE_FORMAT.format(new Date());

        // 获取调用信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3]; // 调用者信息在栈跟踪中的第4个元素（索引为3）
        String className = caller.getClassName();
        int lineNumber = caller.getLineNumber();

        // 格式化日志消息
        return String.format("%s [%s] %s.%s:%d - %s",
                timestamp,
                level,
                className,
                caller.getMethodName(),
                lineNumber,
                message);
    }

    private static void consoleLog(String message, String info) {
        System.out.println(formatLogMessage(message, info));
    }
    public static void info(String message) {
        IDEA_LOGGER.info(message);
        consoleLog(message, "INFO");
    }


    public static void debug(String message) {
        IDEA_LOGGER.debug(message);
        consoleLog(message, "DEBUG");
    }

    public static void error(String message) {
        IDEA_LOGGER.error(message);
    }

    public static void error(String message, Throwable e) {
        IDEA_LOGGER.error(message, e);
    }

    public static void warn(String message) {
        IDEA_LOGGER.warn(message);
        consoleLog(message, "WARN");
    }
}
