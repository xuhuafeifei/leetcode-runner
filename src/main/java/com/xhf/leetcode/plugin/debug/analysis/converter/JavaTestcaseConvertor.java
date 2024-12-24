package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.VariableConvertor;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.ConverterFactory;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * 测试案例转换器. 通过AnalysisResult, 将测试案例转换为Java代码
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaTestcaseConvertor extends AbstractTestcaseConvertor {

    public JavaTestcaseConvertor(String instanceName, AnalysisResult result, Project project) {
        super(instanceName, result, project);
    }

    @Override
    protected String doConvert(String[] testCases, StringBuilder sb) {
        sb.append(instanceName).append(".").append(result.getMethodName()).append("(");
        // 添加方法调用
        for (int i = 0; i < varNames.length; i++) {
            String varName = varNames[i];
            sb.append(varName);
            if (i != varNames.length - 1) {
                sb.append(",");
            } else {
                sb.append(");").append("\r\n");
            }
        }
        return sb.toString();
    }

    @Deprecated // for test
    public JavaTestcaseConvertor(String instanceName, AnalysisResult result) {
        super(instanceName, result, null);
    }
}
