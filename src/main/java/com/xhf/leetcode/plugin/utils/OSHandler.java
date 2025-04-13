package com.xhf.leetcode.plugin.utils;

import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 屏蔽操作系统带来的差异
 */
public class OSHandler {

    public static boolean isWin() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }

    public static boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac");
    }

    public static String getJava(String JAVA_HOME) {
        if (isWin()) {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("java.exe").build();
        } else {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("java").build();
        }
    }

    public static String getJavaName() {
        if (isWin()) {
            return "java.exe";
        } else {
            return "java";
        }
    }

    public static String getJavac(String JAVA_HOME) {
        if (isWin()) {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("javac.exe").build();
        } else {
            return new FileUtils.PathBuilder(JAVA_HOME).append("bin").append("javac").build();
        }
    }

    public static String getJavacName() {
        if (isWin()) {
            return "javac.exe";
        } else {
            return "javac";
        }
    }

    public static boolean isPythonInterpreter(String pythonPath) {
        if (isWin()) {
            return pythonPath.endsWith("python.exe") || pythonPath.endsWith("python3.exe") ||
                    pythonPath.endsWith("python2.exe") || pythonPath.endsWith("python");
        } else {
            // 判断是否存在python, python3, python2
            if (pythonPath.endsWith("python3") || pythonPath.endsWith("python2") || pythonPath.endsWith("python")
                    || pythonPath.endsWith("python3.exe") || pythonPath.endsWith("python2.exe") || pythonPath.endsWith("python.exe")
            ) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean isGPP(String gppPath) {
        if (isWin()) {
            return gppPath.endsWith("g++.exe") || gppPath.endsWith("clang++.exe") || gppPath.endsWith("clang.exe") ||
                    gppPath.endsWith("cc.exe") || gppPath.endsWith("c++.exe") || gppPath.endsWith("g++");
        } else {
            return gppPath.endsWith("g++") || gppPath.endsWith("clang++") || gppPath.endsWith("clang") || gppPath.endsWith("cc") || gppPath.endsWith("c++")
                   || gppPath.endsWith("g++.exe") ;
        }
    }

    public static boolean isGDB(String gdbPath) {
        if (isWin()) {
            return gdbPath.endsWith("gdb.exe") || gdbPath.endsWith("gdb-multiarch.exe") || gdbPath.endsWith("gdb") || gdbPath.endsWith("gdb-multiarch");
        } else {
            return gdbPath.endsWith("gdb") || gdbPath.endsWith("gdb-multiarch") || gdbPath.endsWith("gdb.exe") || gdbPath.endsWith("gdb-multiarch.exe");
        }
    }

    public static String getPython(String pythonPath) {
        if (isWin()) {
            return new FileUtils.PathBuilder(pythonPath).append("python.exe").build();
        } else {
            // 判断是否存在python, python3, python2
            if (new FileUtils.PathBuilder(pythonPath).append("python3").exists()) {
                return new FileUtils.PathBuilder(pythonPath).append("python3").build();
            } else if (new FileUtils.PathBuilder(pythonPath).append("python2").exists()) {
                return new FileUtils.PathBuilder(pythonPath).append("python2").build();
            }
            return new FileUtils.PathBuilder(pythonPath).append("python").build();
        }
    }

    public static String getPythonName() {
        if (isWin()) {
            return "python.exe";
        } else {
            return "python";
        }
    }

    public static Process buildProcess(String cmd) throws IOException {
        if (isWin()) {
            return DebugUtils.buildProcess("cmd.exe", "/c", cmd);
        } else {
            return DebugUtils.buildProcess("/bin/bash", "-c", cmd);
        }
    }

    public static boolean isPath(String content) {
        // 预处理：将反斜杠替换为正斜杠（适用于跨平台场景）
        content = content.replace("\\", "/");

        String regex;
        if (isWin()) {
            // 定义正则表达式
            regex = "^[a-zA-Z]:/[^<>:\"|?*]+(/[^<>:\"|?*]+)*$"; // Windows路径（修复后）
        } else {
            regex = "^/[^<>:\"|?*]+(/[^<>:\"|?*]+)*$"; // Unix/Linux/Mac路径
        }

        // 编译正则表达式，提高性能
        Pattern pattern = Pattern.compile(regex);

        // 匹配路径
        return pattern.matcher(content).matches();
    }

    public static String joinWithoutQuotes(String separator, String... args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(separator);
        }
        return sb.toString().trim();
    }

    public static String joinWithQuotes(String separator, String... args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append("\"").append(arg).append("\"").append(separator);
        }
        return sb.toString().trim();
    }

    public static String join(String separator, String... args) {
        if (isWin()) {
            return joinWithQuotes(separator, args);
        } else {
            return joinWithoutQuotes(separator, args);
        }
    }

    public static String chooseCompliedFile(String winFile, String linuxFile, String macFile) {
        if (isWin()) {
            return winFile;
        } else if (isMac()) {
            return macFile;
        } else {
            return linuxFile;
        }
    }

    public static String chooseCompliedFile(String winFile, String linuxFile) {
        if (isWin()) {
            return winFile;
        } else {
            return linuxFile;
        }
    }
}
