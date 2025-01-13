package com.xhf.leetcode.plugin.debug.analysis.converter;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.ConverterFactory;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.VariableConvertor;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
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
     * 处理多轮solution方法调用的testcases
     *
     * @param testcases 测试案例
     * @return 转换后的代码
     */
    @Deprecated
    @Override
    public String convert(String testcases) {
        testcases = testcases.trim();
        String[] split = testcases.split("\r?\n");
        StringBuilder sb = new StringBuilder();

        // 获取参数个数
        int paramSize = result.getParameterTypes().length;
        // 测试样例的数量存在问题
        if (split.length % paramSize != 0) {
            throw new RuntimeException("测试样例数量有误, 请检查");
        }
        int round = split.length / paramSize;
        // 处理多轮调用
        for (int i = 0; i < round; ++i) {
            // copy 每一轮所需要的测试样例
            String[] sub = new String[paramSize];
            System.arraycopy(split, i * paramSize, sub, 0, paramSize);
            String res = convert(sub);
            sb.append(res);
        }
        return sb.toString();

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
            throw new DebugError("测试样例数量与" + result.getMethodName() + "入参数量不匹配, 请检查!\r\n测试样例个数 = "
                    + len + " " + result.getMethodName() + "入参个数 = " + result.getParameterTypes().length);
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
            throw new DebugError("不支持的方法入参类型: " + paramType);
        }
        return cc.convert(testcase, vName());
    }
}
