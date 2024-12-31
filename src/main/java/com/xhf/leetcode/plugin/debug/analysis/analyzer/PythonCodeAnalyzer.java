package com.xhf.leetcode.plugin.debug.analysis.analyzer;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AbstractCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonCodeAnalyzer extends AbstractCodeAnalyzer {
    public PythonCodeAnalyzer(Project project) {
        super(project);
    }

    /*
        (\w+) 捕获组, 匹配字母数字下划线
     */
    private static final String methodPattern = "def\\s+(\\w+)\\s*\\(([^)]*)\\)";

    private static final Pattern pattern  = Pattern.compile(methodPattern);

    @Override
    public AnalysisResult analyze(String code) {
        LogUtils.simpleDebug(code);
        // 这里需要对code进行处理, 否则遇到如下代码会存在问题
        /*
        #do not modify or remove anything between start-line and end-line
        #lc-start-line
        # Definition for singly-linked list.
        # class ListNode:
        #     def __init__(self, val=0, next=None):
        #         self.val = val
        #         self.next = next
        # Definition for a binary tree node.
        # class TreeNode:
        #     def __init__(self, val=0, left=None, right=None):
        #         self.val = val
        #         self.left = left
        #         self.right = right
        class Solution:
            def isSubPath(self, head: Optional[ListNode], root: Optional[TreeNode]) -> bool:

        #lc-end-line
         */
        String processCode = handlePythonCode(code);
        // 正则表达式匹配方法签名
        Matcher matcher = pattern.matcher(processCode);

        if (matcher.find()) {
            String methodName = matcher.group(1);  // 获取方法名
            String parameters = matcher.group(2);  // 获取参数列表

            // 解析参数类型
            List<String> parameterTypes = new ArrayList<>();
            String[] parametersArray = parameters.split("\\s*,\\s*");
            for (String param : parametersArray) {
                // 提取类型部分
                String[] parts = param.split(":");
                if (parts.length > 1) {
                    parameterTypes.add(parts[1].trim()); // 只获取类型
                }
            }

            return new AnalysisResult(methodName, parameterTypes.toArray(new String[0]));
        }

        throw new DebugError("代码片段分析错误! 无法匹配任何有效信息\n processCode = " + processCode);
    }

    private String handlePythonCode(String code) {
        // 遍历所有行, 移除所有包含首列注释的内容
        StringBuilder sb = new StringBuilder();
        for (String line : code.split("\n")) {
            // 移除行中的注释
            int commentIndex = line.indexOf("#");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
