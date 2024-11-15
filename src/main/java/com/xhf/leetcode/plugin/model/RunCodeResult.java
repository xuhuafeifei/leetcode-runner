/**
 * Copyright 2024 json.cn
 */
package com.xhf.leetcode.plugin.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Auto-generated: 2024-11-13 19:57:1
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/
 */
public class RunCodeResult {

    @SerializedName("lang")
    private String lang;
    @SerializedName("run_success")
    private boolean runSuccess;
    @SerializedName("status_runtime")
    private String statusRuntime;
    @SerializedName("memory")
    private long memory;
    @SerializedName("display_runtime")
    private String displayRuntime;
    @SerializedName("code_answer")
    private List<String> codeAnswer;
    @SerializedName("code_output")
    private List<String> codeOutput;
    @SerializedName("std_output_list")
    private List<String> stdOutputList;
    @SerializedName("expected_code_answer")
    private List<String> expectedCodeAnswer;
    @SerializedName("expected_code_output")
    private List<String> expectedCodeOutput;
    @SerializedName("expected_std_output_list")
    private List<String> expectedStdOutputList;
    @SerializedName("full_runtime_error")
    private String fullRuntimeError;
    @SerializedName("full_compile_error")
    private String fullCompileError;
    @SerializedName("correct_answer")
    private boolean correctAnswer;
    @SerializedName("status_msg")
    private String statusMsg;
    @SerializedName("status_memory")
    private String statusMemory;
    @SerializedName("total_correct")
    private String totalCorrect;
    @SerializedName("total_testcases")
    private String totalTestcases;
    /*
    Compare each test case running result.
    For example, if there are 3 cases, and the res is true false true,
    then the compareResult will be "101"
    if the res is true true true, the compareResult will be "111"
    */
    @SerializedName("compare_result")
    private String compareResult;

    public void setFullCompileError(String fullCompileError) {
        this.fullCompileError = fullCompileError;
    }

    public String getFullCompileError() {
        return fullCompileError;
    }

    public void setCompareResult(String compareResult) {
        this.compareResult = compareResult;
    }

    public String getCompareResult() {
        return this.compareResult;
    }

    public void setTotalTestcases(String totalTestcases) {
        this.totalTestcases = totalTestcases;
    }
    public String getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalCorrect(String totalCorrect) {
        this.totalCorrect = totalCorrect;
    }
    public String getTotalCorrect() {
        return totalCorrect;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    public String getLang() {
        return lang;
    }

    public void setRunSuccess(boolean runSuccess) {
        this.runSuccess = runSuccess;
    }
    public boolean getRunSuccess() {
        return runSuccess;
    }

    public void setStatusRuntime(String statusRuntime) {
        this.statusRuntime = statusRuntime;
    }
    public String getStatusRuntime() {
        return statusRuntime;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }
    public long getMemory() {
        return memory;
    }

    public void setDisplayRuntime(String displayRuntime) {
        this.displayRuntime = displayRuntime;
    }
    public String getDisplayRuntime() {
        return displayRuntime;
    }

    public void setCodeAnswer(List<String> codeAnswer) {
        this.codeAnswer = codeAnswer;
    }
    public List<String> getCodeAnswer() {
        return codeAnswer;
    }

    public void setCodeOutput(List<String> codeOutput) {
        this.codeOutput = codeOutput;
    }
    public List<String> getCodeOutput() {
        return codeOutput;
    }

    public void setStdOutputList(List<String> stdOutputList) {
        this.stdOutputList = stdOutputList;
    }
    public List<String> getStdOutputList() {
        return stdOutputList;
    }

    public void setExpectedCodeAnswer(List<String> expectedCodeAnswer) {
        this.expectedCodeAnswer = expectedCodeAnswer;
    }
    public List<String> getExpectedCodeAnswer() {
        return expectedCodeAnswer;
    }

    public void setExpectedCodeOutput(List<String> expectedCodeOutput) {
        this.expectedCodeOutput = expectedCodeOutput;
    }
    public List<String> getExpectedCodeOutput() {
        return expectedCodeOutput;
    }

    public void setExpectedStdOutputList(List<String> expectedStdOutputList) {
        this.expectedStdOutputList = expectedStdOutputList;
    }
    public List<String> getExpectedStdOutputList() {
        return expectedStdOutputList;
    }

    public void setFullRuntimeError(String fullRuntimeError) {
        this.fullRuntimeError = fullRuntimeError;
    }
    public String getFullRuntimeError() {
        return fullRuntimeError;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    public boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMemory(String statusMemory) {
        this.statusMemory = statusMemory;
    }
    public String getStatusMemory() {
        return statusMemory;
    }

}