package com.xhf.leetcode.plugin.utils;

import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;

import java.io.IOException;

/**
 * 屏蔽操作系统带来的差异
 */
public class OSHandler {

    public static String getJava(String JAVA_HOME) {
        if (FileUtils.isWin()) {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("java.exe").build();
        } else {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("java").build();
        }
    }

    public static String getJavaName() {
        if (FileUtils.isWin()) {
            return "java.exe";
        } else {
            return "java";
        }
    }

    public static String getJavac(String JAVA_HOME) {
        if (FileUtils.isWin()) {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("javac.exe").build();
        } else {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("javac").build();
        }
    }

    public static String getJavacName() {
        if (FileUtils.isWin()) {
            return "javac.exe";
        } else {
            return "javac";
        }
    }

    public static String getPython(String pythonPath) {
        if (FileUtils.isWin()) {
            return new FileUtils.PathBuilder(pythonPath).append("python.exe").build();
        } else {
            return new FileUtils.PathBuilder(pythonPath).append("python").build();
        }
    }

    public static String getPythonName() {
        if (FileUtils.isWin()) {
            return "python.exe";
        } else {
            return "python";
        }
    }

    public static Process buildProcess(String cmd) throws IOException {
        if (FileUtils.isWin()) {
            return DebugUtils.buildProcess("cmd.exe", "/c", cmd);
        } else {
            return DebugUtils.buildProcess("/bin/bash", "-c", cmd);
        }
    }
}
