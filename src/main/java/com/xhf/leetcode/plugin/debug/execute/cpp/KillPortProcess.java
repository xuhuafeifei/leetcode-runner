package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.UnSafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class KillPortProcess {

    @UnSafe("当前方法会强制杀死占用port的端口, 如果指定的是系统端口, 可能会导致操作系统的瘫痪")
    public static void killProcess(int port) {
        try {
            // 执行 netstat 命令找到占用端口的PID
            String cmd = " netstat -aon | findstr :" + port;
            Process process = DebugUtils.buildProcess("cmd.exe", "/c", cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            String pid = null;
            while ((line = reader.readLine()) != null) {
                // 解析出PID，假设格式为 "TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       1234"
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 4) {
                    pid = parts[parts.length - 1]; // 获取最后一部分作为PID
                    break;
                }
            }
            reader.close();

            if (pid != null && !pid.isEmpty()) {
                // 使用 taskkill 命令根据PID杀死进程
                String killProcessCommand = "taskkill /PID " + pid + " /F";
                Process killProcess = DebugUtils.buildProcess(killProcessCommand);
                BufferedReader killReader = new BufferedReader(new InputStreamReader(killProcess.getInputStream(), "GBK"));
                String killOutput;
                while ((killOutput = killReader.readLine()) != null) {
                    System.out.println(killOutput);
                }
                killReader.close();
            } else {
                System.out.println("未找到占用端口 " + port + " 的进程。");
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

}