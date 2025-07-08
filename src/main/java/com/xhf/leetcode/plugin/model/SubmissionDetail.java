package com.xhf.leetcode.plugin.model;

public class SubmissionDetail {

    private String code;
    private Object fullCodeOutput;
    private boolean isMine;
    private String lang;
    private String langVerboseName;
    private String memory;
    private String memoryDisplay;
    private double memoryPercentile;
    private OutputDetail outputDetail;
    private long passedTestCaseCnt;
    private Question question;
    private String runtimeDisplay;
    private double runtimePercentile;
    private String statusDisplay;
    private String stdOutput;
    private Object submissionComment;
    private Object testBodies;
    private Object testDescriptions;
    private Object testInfo;
    private long timestamp;
    private long totalTestCaseCnt;

    public String getCode() {
        return code;
    }

    public void setCode(String value) {
        this.code = value;
    }

    public Object getFullCodeOutput() {
        return fullCodeOutput;
    }

    public void setFullCodeOutput(Object value) {
        this.fullCodeOutput = value;
    }

    public boolean getIsMine() {
        return isMine;
    }

    public void setIsMine(boolean value) {
        this.isMine = value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String value) {
        this.lang = value;
    }

    public String getLangVerboseName() {
        return langVerboseName;
    }

    public void setLangVerboseName(String value) {
        this.langVerboseName = value;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String value) {
        this.memory = value;
    }

    public String getMemoryDisplay() {
        return memoryDisplay;
    }

    public void setMemoryDisplay(String value) {
        this.memoryDisplay = value;
    }

    public double getMemoryPercentile() {
        return memoryPercentile;
    }

    public void setMemoryPercentile(double value) {
        this.memoryPercentile = value;
    }

    public OutputDetail getOutputDetail() {
        return outputDetail;
    }

    public void setOutputDetail(OutputDetail value) {
        this.outputDetail = value;
    }

    public long getPassedTestCaseCnt() {
        return passedTestCaseCnt;
    }

    public void setPassedTestCaseCnt(long value) {
        this.passedTestCaseCnt = value;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question value) {
        this.question = value;
    }

    public String getRuntimeDisplay() {
        return runtimeDisplay;
    }

    public void setRuntimeDisplay(String value) {
        this.runtimeDisplay = value;
    }

    public double getRuntimePercentile() {
        return runtimePercentile;
    }

    public void setRuntimePercentile(double value) {
        this.runtimePercentile = value;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String value) {
        this.statusDisplay = value;
    }

    public String getStdOutput() {
        return stdOutput;
    }

    public void setStdOutput(String value) {
        this.stdOutput = value;
    }

    public Object getSubmissionComment() {
        return submissionComment;
    }

    public void setSubmissionComment(Object value) {
        this.submissionComment = value;
    }

    public Object getTestBodies() {
        return testBodies;
    }

    public void setTestBodies(Object value) {
        this.testBodies = value;
    }

    public Object getTestDescriptions() {
        return testDescriptions;
    }

    public void setTestDescriptions(Object value) {
        this.testDescriptions = value;
    }

    public Object getTestInfo() {
        return testInfo;
    }

    public void setTestInfo(Object value) {
        this.testInfo = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long value) {
        this.timestamp = value;
    }

    public long getTotalTestCaseCnt() {
        return totalTestCaseCnt;
    }

    public void setTotalTestCaseCnt(long value) {
        this.totalTestCaseCnt = value;
    }
}