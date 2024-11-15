package com.xhf.learning.httpclient;

import java.util.List;

/**
 *
 */
public class CodeRunInfo {

    private int statusCode;
    private String lang;
    private boolean runSuccess;
    private String runtimeError;
    private String fullRuntimeError;
    private String statusRuntime;
    private long memory;
    private List<String> codeAnswer;
    private List<String> codeOutput;
    private List<String> stdOutputList;
    private int elapsedTime;
    private long taskFinishTime;
    private String taskName;
    private int expectedStatusCode;
    private String expectedLang;
    private boolean expectedRunSuccess;
    private String expectedStatusRuntime;
    private long expectedMemory;
    private String expectedDisplayRuntime;
    private List<String> expectedCodeAnswer;
    private List<String> expectedCodeOutput;
    private List<String> expectedStdOutputList;
    private int expectedElapsedTime;
    private long expectedTaskFinishTime;
    private String expectedTaskName;
    private boolean correctAnswer;
    private String compareResult;
    private String statusMsg;
    private String state;
    private boolean fastSubmit;
    private int totalCorrect;
    private int totalTestcases;
    private String submissionId;
    private String runtimePercentile;
    private String statusMemory;
    private String memoryPercentile;
    private String prettyLang;
    public void setStatusCode(int statusCode) {
         this.statusCode = statusCode;
     }
     public int getStatusCode() {
         return statusCode;
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

    public void setRuntimeError(String runtimeError) {
         this.runtimeError = runtimeError;
     }
     public String getRuntimeError() {
         return runtimeError;
     }

    public void setFullRuntimeError(String fullRuntimeError) {
         this.fullRuntimeError = fullRuntimeError;
     }
     public String getFullRuntimeError() {
         return fullRuntimeError;
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

    public void setElapsedTime(int elapsedTime) {
         this.elapsedTime = elapsedTime;
     }
     public int getElapsedTime() {
         return elapsedTime;
     }

    public void setTaskFinishTime(long taskFinishTime) {
         this.taskFinishTime = taskFinishTime;
     }
     public long getTaskFinishTime() {
         return taskFinishTime;
     }

    public void setTaskName(String taskName) {
         this.taskName = taskName;
     }
     public String getTaskName() {
         return taskName;
     }

    public void setExpectedStatusCode(int expectedStatusCode) {
         this.expectedStatusCode = expectedStatusCode;
     }
     public int getExpectedStatusCode() {
         return expectedStatusCode;
     }

    public void setExpectedLang(String expectedLang) {
         this.expectedLang = expectedLang;
     }
     public String getExpectedLang() {
         return expectedLang;
     }

    public void setExpectedRunSuccess(boolean expectedRunSuccess) {
         this.expectedRunSuccess = expectedRunSuccess;
     }
     public boolean getExpectedRunSuccess() {
         return expectedRunSuccess;
     }

    public void setExpectedStatusRuntime(String expectedStatusRuntime) {
         this.expectedStatusRuntime = expectedStatusRuntime;
     }
     public String getExpectedStatusRuntime() {
         return expectedStatusRuntime;
     }

    public void setExpectedMemory(long expectedMemory) {
         this.expectedMemory = expectedMemory;
     }
     public long getExpectedMemory() {
         return expectedMemory;
     }

    public void setExpectedDisplayRuntime(String expectedDisplayRuntime) {
         this.expectedDisplayRuntime = expectedDisplayRuntime;
     }
     public String getExpectedDisplayRuntime() {
         return expectedDisplayRuntime;
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

    public void setExpectedElapsedTime(int expectedElapsedTime) {
         this.expectedElapsedTime = expectedElapsedTime;
     }
     public int getExpectedElapsedTime() {
         return expectedElapsedTime;
     }

    public void setExpectedTaskFinishTime(long expectedTaskFinishTime) {
         this.expectedTaskFinishTime = expectedTaskFinishTime;
     }
     public long getExpectedTaskFinishTime() {
         return expectedTaskFinishTime;
     }

    public void setExpectedTaskName(String expectedTaskName) {
         this.expectedTaskName = expectedTaskName;
     }
     public String getExpectedTaskName() {
         return expectedTaskName;
     }

    public void setCorrectAnswer(boolean correctAnswer) {
         this.correctAnswer = correctAnswer;
     }
     public boolean getCorrectAnswer() {
         return correctAnswer;
     }

    public void setCompareResult(String compareResult) {
         this.compareResult = compareResult;
     }
     public String getCompareResult() {
         return compareResult;
     }

    public void setStatusMsg(String statusMsg) {
         this.statusMsg = statusMsg;
     }
     public String getStatusMsg() {
         return statusMsg;
     }

    public void setState(String state) {
         this.state = state;
     }
     public String getState() {
         return state;
     }

    public void setFastSubmit(boolean fastSubmit) {
         this.fastSubmit = fastSubmit;
     }
     public boolean getFastSubmit() {
         return fastSubmit;
     }

    public void setTotalCorrect(int totalCorrect) {
         this.totalCorrect = totalCorrect;
     }
     public int getTotalCorrect() {
         return totalCorrect;
     }

    public void setTotalTestcases(int totalTestcases) {
         this.totalTestcases = totalTestcases;
     }
     public int getTotalTestcases() {
         return totalTestcases;
     }

    public void setSubmissionId(String submissionId) {
         this.submissionId = submissionId;
     }
     public String getSubmissionId() {
         return submissionId;
     }

    public void setRuntimePercentile(String runtimePercentile) {
         this.runtimePercentile = runtimePercentile;
     }
     public String getRuntimePercentile() {
         return runtimePercentile;
     }

    public void setStatusMemory(String statusMemory) {
         this.statusMemory = statusMemory;
     }
     public String getStatusMemory() {
         return statusMemory;
     }

    public void setMemoryPercentile(String memoryPercentile) {
         this.memoryPercentile = memoryPercentile;
     }
     public String getMemoryPercentile() {
         return memoryPercentile;
     }

    public void setPrettyLang(String prettyLang) {
         this.prettyLang = prettyLang;
     }
     public String getPrettyLang() {
         return prettyLang;
     }

}