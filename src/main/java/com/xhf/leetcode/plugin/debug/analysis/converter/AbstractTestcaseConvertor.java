package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.ConverterFactory;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.VariableConvertor;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

/**
 * 测试案例转换器, 可以将测试案例转换为对应语言的代码
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public abstract class AbstractTestcaseConvertor implements TestcaseConvertor{
    protected final String instanceName;
    protected final AnalysisResult result;
    private final ConverterFactory cf;
    private int count = 0;

    private final Project project;
    protected String[] varNames;

    public AbstractTestcaseConvertor(String instanceName, AnalysisResult result, Project project) {
        this.instanceName = instanceName;
        this.result = result;
        this.cf = ConverterFactory.getInstance();
        this.project = project;
    }

    /**
     * 自动获取当前打文件处理的question的测试样例, 并转换为对应的调用代码
     * @return 转换后的代码
     */
    @Override
    public String autoConvert() throws DebugError {
        // 获取测试案例
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByCurrentVFile(project);
        String tests = lc.getDebugTestcase().trim();
        return convert(tests.split("\n"));
    }

    /**
     * 只负责处理一轮solution方法调用的testcases
     * 比如solution.twoSum(int, int). testcases只能有两个测试案例. 处理完成后, 则为一轮
     *
     * @param testCases 测试案例
     * @return 转换后的代码
     */
    @Override
    public String convert(String[] testCases) throws DebugError {
        int len = testCases.length;
        // 判断测试输入和参数个数是否匹配
        if (len != result.getParameterTypes().length) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.testcase.number") + " " + result.getMethodName() + " " + BundleUtils.i18n("debug.leetcode.input.param.number")
                    + " " + BundleUtils.i18n("debug.leetcode.notmatch") + "\r\n" +
                    BundleUtils.i18n("debug.leetcode.testcase.number") + " = " + len + "\n"
                    + result.getMethodName() + BundleUtils.i18n("debug.leetcode.input.param.number") + " = " + result.getParameterTypes().length);
        }
        // 参数类型
        this.varNames = new String[len];
        StringBuilder sb = new StringBuilder();
        // 添加变量创建
        for (int i = 0; i < len; i++) {
            sb.append(createVariable(testCases[i], result.getParameterTypes()[i]));
            // 存储变量名
            varNames[i] = vName();
            updateVName();
        }
        return sb.append(createInstanceCallCode(varNames, instanceName)).toString();
    }

    private void updateVName() {
        count += 1;
    }

    /**
     * 处理调用代码的转换
     * @param varNames 参数变量名
     * @param instanceName 实例名
     * @return String
     */
    protected abstract String createInstanceCallCode(String[] varNames, String instanceName);

    /**
     * 获取变量名
     * @return vName
     */
    protected String vName() {
        String variableName = "a";
        return variableName + count;
    }

    /**
     * 将testcase转换为Java代码
     * 比如 "[1,2,3]" 转换为 "int[] a{count} = new int[]{1,2,3}"
     * @param testcase 测试案例
     * @param paramType 入参类型
     * @return 转换后的代码
     */
    protected String createVariable(String testcase, String paramType) {
        VariableConvertor cc = cf.createVariableConvertor(paramType);
        if (cc == null) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.notsupport.param.type") + ": " + paramType);
        }
        return cc.convert(testcase, vName());
    }
}
