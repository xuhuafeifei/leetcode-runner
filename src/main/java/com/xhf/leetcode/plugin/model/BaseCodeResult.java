package com.xhf.leetcode.plugin.model;

import com.google.gson.annotations.SerializedName;

/**
 * extract base info from the {@link RunCodeResult} result and {@link SubmitCodeResult} result
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class BaseCodeResult {

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
    @SerializedName("full_runtime_error")
    private String fullRuntimeError;
    @SerializedName("status_msg")
    private String statusMsg;
    @SerializedName("status_memory")
    private String statusMemory;
    @SerializedName("full_compile_error")
    private String fullCompileError;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean getRunSuccess() {
        return runSuccess;
    }

    public void setRunSuccess(boolean runSuccess) {
        this.runSuccess = runSuccess;
    }

    public String getStatusRuntime() {
        return statusRuntime;
    }

    public void setStatusRuntime(String statusRuntime) {
        this.statusRuntime = statusRuntime;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public String getDisplayRuntime() {
        return displayRuntime;
    }

    public void setDisplayRuntime(String displayRuntime) {
        this.displayRuntime = displayRuntime;
    }

    public String getCompareResult() {
        return this.compareResult;
    }

    public void setCompareResult(String compareResult) {
        this.compareResult = compareResult;
    }

    public String getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalTestcases(String totalTestcases) {
        this.totalTestcases = totalTestcases;
    }

    public String getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(String totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public String getFullCompileError() {
        return fullCompileError;
    }

    public void setFullCompileError(String fullCompileError) {
        this.fullCompileError = fullCompileError;
    }

    public String getFullRuntimeError() {
        return fullRuntimeError;
    }

    public void setFullRuntimeError(String fullRuntimeError) {
        this.fullRuntimeError = fullRuntimeError;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getStatusMemory() {
        return statusMemory;
    }

    public void setStatusMemory(String statusMemory) {
        this.statusMemory = statusMemory;
    }
}
