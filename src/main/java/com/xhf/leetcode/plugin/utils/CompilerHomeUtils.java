package com.xhf.leetcode.plugin.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author 文艺倾年
 */
public class CompilerHomeUtils {


    public static void main(String[] args) {
        String winCommand = "where java";
        String otherCommand = "where java";

        String processFilePath = "E:/data";

        String osType = System.getProperty("os.name").toLowerCase();

        String command = osType.contains("win") ? winCommand : otherCommand;

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(null);

        try {
            Process process = processBuilder.start();
            // 读取命令的输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行完成，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
