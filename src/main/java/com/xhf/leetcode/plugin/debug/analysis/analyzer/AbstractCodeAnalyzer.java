package com.xhf.leetcode.plugin.debug.analysis.analyzer;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.CodeService;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * 抽象代码分析器, 分析核心代码片段, 返回分析结果{@link AnalysisResult}
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractCodeAnalyzer implements CodeAnalyzer{
    private final Project project;

    public AbstractCodeAnalyzer(Project project) {
        this.project = project;
    }

    /**
     * 自动获取当前打开题目的核心代码, 分析代码片段
     * @return 分析结果
     */
    public AnalysisResult autoAnalyze() {
        // 从当前打开的VFile路径名称中解析出当前题目的titleSlug
        String titleSlug = CodeService.getInstance(project).parseTitleSlugFromVFile(ViewUtils.getCurrentOpenVirtualFile(project));
        LogUtils.simpleDebug("titleSlug = " + titleSlug);
        Question question = QuestionService.getInstance().queryQuestionInfo(titleSlug, project);
        return analyze(question.getCodeSnippets());
    }

    /**
     * 分析核心代码片段
     * @param code 核心代码片段
     * @return 分析结果
     */
    public abstract AnalysisResult analyze(String code);
}
