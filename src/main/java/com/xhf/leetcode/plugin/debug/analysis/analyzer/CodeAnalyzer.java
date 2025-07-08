package com.xhf.leetcode.plugin.debug.analysis.analyzer;

import com.xhf.leetcode.plugin.exception.DebugError;

public interface CodeAnalyzer {

    /**
     * 自动获取当前打开题目的核心代码, 分析代码片段
     *
     * @return 分析结果
     */
    AnalysisResult autoAnalyze() throws DebugError;

    /**
     * 分析核心代码片段
     *
     * @param code 核心代码片段
     * @return 分析结果
     */
    AnalysisResult analyze(String code) throws DebugError;
}
