package com.xhf.leetcode.plugin.model;

import com.google.gson.annotations.SerializedName;

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

    public void setFullCompileError(String fullCompileError) {
        this.fullCompileError = fullCompileError;
    }

    public String getFullCompileError() {
        return fullCompileError;
    }

    public void setFullRuntimeError(String fullRuntimeError) {
        this.fullRuntimeError = fullRuntimeError;
    }
    public String getFullRuntimeError() {
        return fullRuntimeError;
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