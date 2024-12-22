package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;
import com.sun.jdi.event.StepEvent;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 执行W操作. 操作具体信息详见{@link com.xhf.leetcode.plugin.debug.params.Operation}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaWInst implements InstExecutor{

    public JavaWInst() {
    }

    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        Location location = context.getLocation();
        int lineNumber = location.lineNumber(); // 行号

        String res = DebugUtils.buildCurrentLineInfoByLocation(location);

        try {
            String sourceFile = location.sourceName();  // 获取源文件名
            sourceFile = new FileUtils.PathBuilder(context.getEnv().getFilePath()).append(sourceFile).build();
            String sourceCode = getSourceCodeAtLocation(sourceFile, lineNumber);  // 获取源代码内容
            res += "\n" + sourceCode;
        } catch (AbsentInformationException e) {
            throw new RuntimeException(e);
        }

        ExecuteResult success = ExecuteResult.success(inst.getOperation(), res);
        // 填充内容
        DebugUtils.fillExecuteResultByLocation(success, location);
        return success;
    }

    // 通过文件路径和行号获取指定位置的源代码行内容
    private String getSourceCodeAtLocation(String sourceFile, int lineNumber) {
        Path filePath = Paths.get(sourceFile);  // 获取源文件路径
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // 跳过文件的前面若干行，直到找到目标行
            for (int i = 1; i < lineNumber; i++) {
                reader.readLine();  // 跳过前面不需要的行
            }

            // 读取目标行并返回
            String currentLine = reader.readLine();
            if (currentLine != null) {
                return currentLine;  // 返回当前行的源代码
            }
        } catch (IOException e) {
            LogUtils.simpleDebug("Error reading source file: " + e.getMessage());
        }

        return "Source code not available";  // 如果读取失败，返回提示信息
    }
}
