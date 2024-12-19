package com.xhf.leetcode.plugin.debug.utils;


import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
}
