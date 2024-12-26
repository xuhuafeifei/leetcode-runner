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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final StringBuilder sb = new StringBuilder();

    private static String formatLogMessage(String message, String level, int idx) {
        sb.delete(0, sb.length());
        // 获取当前时间
        String timestamp = DATE_FORMAT.format(new Date());

        // 获取调用信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        idx = idx + 1;
        StackTraceElement caller = stackTrace[idx];

        String prefix = String.format("%s [%s] ", timestamp, level);
        String whiteSpace = String.format("%" + prefix.length() + "s", "");
        sb.append(prefix);
        sb.append(caller).append(" ").append(message).append("\n");

        for (int i = 1; i < 3 && idx + i < stackTrace.length; ++i) {
            sb.append(whiteSpace);
            sb.append(stackTrace[idx + i]);
            sb.append("\n");
        }

        // 格式化日志消息
        return sb.toString();
    }

    private static void consoleLog(String message, String info) {
        System.out.print(formatLogMessage(message, info, 3));
    }
    public static void info(String message) {
        IDEA_LOGGER.info(message);
        consoleLog(message, "INFO");
    }


    public static void debug(String message) {
        IDEA_LOGGER.debug(message);
        consoleLog(message, "DEBUG");
    }

    public static void simpleDebug(String message) {
        simpleConsoleLog(message, "DEBUG");
    }

    private static void simpleConsoleLog(String message, String level) {
        // 获取当前时间
        String timestamp = DATE_FORMAT.format(new Date());
        String formatted = String.format("%s [%s] %s", timestamp, level, message);
        System.out.println(formatted);
        IDEA_LOGGER.debug(formatted);
    }


    public static void error(String message) {
        IDEA_LOGGER.error(message);
    }

    public static void error(Throwable e) {
        IDEA_LOGGER.error(e);
        e.printStackTrace();
    }

    public static void error(String message, Throwable e) {
        IDEA_LOGGER.error(message, e);
    }

    public static void warn(String message) {
        IDEA_LOGGER.warn(message);
        consoleLog(message, "WARN");
    }
}
