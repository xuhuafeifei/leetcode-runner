package com.xhf.leetcode.plugin.debug.analysis;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.analysis.convert.VariableConvertor;
import com.xhf.leetcode.plugin.debug.analysis.convert.ConverterFactory;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.LeetcodeEditor;
import com.xhf.leetcode.plugin.utils.ViewUtils;
import org.bouncycastle.crypto.agreement.jpake.JPAKERound1Payload;

/**
 * 测试案例转换器. 通过AnalysisResult, 将测试案例转换为Java代码
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaTestcaseConvertor {
    private final String instanceName;
    private final AnalysisResult result;
    private final ConverterFactory cf;
    private int count = 0;
    private final String variableName = "a";
    
    private final Project project;
    
    @Deprecated // for test
    public JavaTestcaseConvertor(String instanceName, AnalysisResult result) {
        this(instanceName, result, null);
    }
    
    public JavaTestcaseConvertor(String instanceName, AnalysisResult result, Project project) {
        this.instanceName = instanceName;
        this.result = result;
        this.cf = ConverterFactory.getInstance();
        this.project = project;
    }

    /**
     * 自动获取当前打文件处理的question的测试样例, 并转换为对应的调用代码
     * @return
     */
    public String autoConvert() {
        // 获取测试案例
        LeetcodeEditor lc = ViewUtils.getLeetcodeEditorByCurrentVFile(project);
        String tests = lc.getExampleTestcases();
        return convert(tests);
    }

    /**
     * 处理多轮solution方法调用的testcases
     *
     * @param testcases
     * @return
     */
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
     * @param testCases
     * @return
     */
    public String convert(String[] testCases) {
        StringBuilder sb = new StringBuilder();
        int len = testCases.length;
        // 参数类型
        String[] pT = result.getParameterTypes();

        String[] varNames = new String[len];
        // 添加变量创建
        for (int i = 0; i < len; i++) {
            sb.append(createVariable(testCases[i], pT[i]));
            // 存储变量名
            varNames[i] = vName();
            count += 1;
        }
        // 添加方法调用
        sb.append(instanceName).append(".").append(result.getMethodName()).append("(");
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

    /**
     * 获取变量名
     * @return
     */
    private String vName() {
        return variableName + count;
    }

    /**
     * 将testcase转换为Java代码
     * 比如 "[1,2,3]" 转换为 "int[] a{count} = new int[]{1,2,3}"
     * @param testcase
     * @param paramType
     * @return
     */
    private String createVariable(String testcase, String paramType) {
        VariableConvertor cc = cf.createVariableConvertor(paramType);
        String res = cc.convert(testcase, vName());
        return res;
    }
}
