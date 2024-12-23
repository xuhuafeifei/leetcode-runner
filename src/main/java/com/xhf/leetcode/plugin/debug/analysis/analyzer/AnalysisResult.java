package com.xhf.leetcode.plugin.debug.analysis.analyzer;

public class AnalysisResult {
    private String methodName;
    private String returnType;
    private String[] parameterTypes;

    // 构造函数
    public AnalysisResult(String methodName, String returnType, String[] parameterTypes) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public AnalysisResult(String methodName, String[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    // Getter 和 Setter 方法
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameterTypes=" + java.util.Arrays.toString(parameterTypes) +
                '}';
    }
}
