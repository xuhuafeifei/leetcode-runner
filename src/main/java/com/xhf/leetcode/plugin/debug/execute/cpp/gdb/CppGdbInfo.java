package com.xhf.leetcode.plugin.debug.execute.cpp.gdb;

import com.google.gson.annotations.SerializedName;

/**
 * 用于存储GDB输出的内容, 存储GDB-MI模式下不同类型的输出
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppGdbInfo {

    @SerializedName("stopped_reason")
    private String stoppedReason;
    @SerializedName("console_output")
    private String consoleOutput;
    @SerializedName("status")
    private String status;
    @SerializedName("log_output")
    private String logOutput;
    @SerializedName("result_record")
    private String resultRecord;

    public String getStoppedReason() {
        return stoppedReason;
    }

    public void setStoppedReason(String stoppedReason) {
        this.stoppedReason = stoppedReason;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public void setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogOutput() {
        return logOutput;
    }

    public void setLogOutput(String logOutput) {
        this.logOutput = logOutput;
    }

    public String getResultRecord() {
        return resultRecord;
    }

    public void setResultRecord(String resultRecord) {
        this.resultRecord = resultRecord;
    }

}
