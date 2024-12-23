package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;

/**
 * 测试案例转换器. 通过AnalysisResult, 将测试案例转换为Java代码
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonTestcaseConvertor extends AbstractTestcaseConvertor {

    public PythonTestcaseConvertor(String instanceName, AnalysisResult result, Project project) {
        super(instanceName, result, project);
    }

    @Deprecated // for test
    public PythonTestcaseConvertor(String instanceName, AnalysisResult result) {
        super(instanceName, result, null);
    }
}
