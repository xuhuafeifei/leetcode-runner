package com.xhf.leetcode.plugin.debug.analysis.analyzer;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppCodeAnalyzer extends AbstractCodeAnalyzer {

    /*
        (\w+) 捕获组, 匹配字母数字下划线
     */
    public static final String methodPattern = ".*?\\s+(\\w+)\\s*\\(([^)]*)\\)";
    public static final Pattern pattern = Pattern.compile(methodPattern);

    public CppCodeAnalyzer(Project project) {
        super(project);
    }

    public AnalysisResult analyze(String code) {
        LogUtils.simpleDebug(code);
        code = handleCode(code);
        // 正则表达式匹配方法签名
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            String methodName = matcher.group(1);  // 获取方法名
            String parameters = matcher.group(2);  // 获取参数列表

            // 解析参数类型
            List<String> parameterTypes = new ArrayList<>();
            String[] parametersArray = parameters.split("\\s*,\\s*");
            for (String param : parametersArray) {
                // 提取类型部分
                String[] parts = param.split("\\s+");
                if (parts.length > 1) {
                    String trim = parts[0].trim();
                    int endIndex = trim.length() - 1;
                    if (trim.lastIndexOf("&") == endIndex) {
                        trim = trim.substring(0, endIndex);
                    }
                    parameterTypes.add(trim); // 只获取类型
                }
            }

            return new AnalysisResult(methodName, parameterTypes.toArray(new String[0]));
        }

        throw new DebugError(BundleUtils.i18n("debug.leetcode.analyzer") + "\n code = \n" + code);
    }

    private String handleCode(String code) {
        StringBuilder sb = new StringBuilder();
        String[] split = code.split("\n");
        int i = 0;
        for (i = 0; i < split.length; i++) {
            String line = split[i];
            if (line.startsWith("class Solution")) {
                break;
            }
        }
        for (int j = i; j < split.length; j++) {
            sb.append(split[j]).append("\n");
        }
        return sb.toString();
    }
}
