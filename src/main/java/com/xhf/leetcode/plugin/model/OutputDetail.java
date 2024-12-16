package com.xhf.leetcode.plugin.model;

public class OutputDetail {
    private String codeOutput;
    private String compileError;
    private String expectedOutput;
    private String input;
    private String lastTestcase;
    private String runtimeError;

    public String getCodeOutput() { return codeOutput; }
    public void setCodeOutput(String value) { this.codeOutput = value; }

    public String getCompileError() { return compileError; }
    public void setCompileError(String value) { this.compileError = value; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String value) { this.expectedOutput = value; }

    public String getInput() { return input; }
    public void setInput(String value) { this.input = value; }

    public String getLastTestcase() { return lastTestcase; }
    public void setLastTestcase(String value) { this.lastTestcase = value; }

    public String getRuntimeError() { return runtimeError; }
    public void setRuntimeError(String value) { this.runtimeError = value; }
}