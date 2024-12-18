package com.xhf.leetcode.plugin.debug.analysis;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.util.regex.*;
import java.util.*;

/**
 * Java代码分析器
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaCodeAnalyzer {

    private final Project project;

    public JavaCodeAnalyzer(Project project) {
        this.project = project;
    }

    @Deprecated // only for test
    public JavaCodeAnalyzer() {
        this.project = null;
    }


    /**
     * 自动获取当前打开题目的核心代码, 分析代码片段
     * @return 分析结果
     */
    public AnalysisResult autoAnalyze() {
        // 从当前打开的VFile路径名称中解析出当前题目的titleSlug
        String titleSlug = CodeService.parseTitleSlugFromVFile(ViewUtils.getCurrentOpenVirtualFile(project));
        Question question = QuestionService.getInstance().queryQuestionInfo(titleSlug, project);
        return analyze(question.getCodeSnippets());
    }

    static final String methodPattern = "public\\s+.*\\s+(\\w+)\\s*\\(([^)]*)\\)";
    private static final Pattern pattern = Pattern.compile(methodPattern);

    /**
     * 分析核心代码片段
     * @param code 核心代码片段
     * @return 分析结果
     */
    public AnalysisResult analyze(String code) {
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
                    parameterTypes.add(parts[0]); // 只获取类型
                }
            }

            return new AnalysisResult(methodName, parameterTypes.toArray(new String[0]));
        }

        return null; // 如果没有匹配到方法签名，返回 null
    }
}
