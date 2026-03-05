package com.xhf.leetcode.plugin.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class SubmitCodeResult extends BaseCodeResult {

    /**
     * which means the submit code answer of last testcase
     */
    @SerializedName("code_output")
    private String codeOutput;

    /**
     * which means the submit code standard output of last testcase
     */
    @SerializedName("std_output")
    private String stdOutput;
    @SerializedName("last_testcase")
    private String lastTestcase;
    /**
     * which means the right answer of last testcase
     */
    @SerializedName("expected_output")
    private String expectedOutput;

    public String getCodeOutput() {
        return codeOutput;
    }

    public void setCodeOutput(String codeOutput) {
        this.codeOutput = codeOutput;
    }

    public String getStdOutput() {
        return stdOutput;
    }

    public void setStdOutput(String stdOutput) {
        this.stdOutput = stdOutput;
    }

    public String getLastTestcase() {
        return lastTestcase;
    }

    public void setLastTestcase(String lastTestcase) {
        this.lastTestcase = lastTestcase;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
