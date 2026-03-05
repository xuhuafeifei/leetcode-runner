/**
 * Copyright 2024 json.cn
 */
package com.xhf.leetcode.plugin.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;


/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RunCodeResult extends BaseCodeResult {

    @SerializedName("code_answer")
    private List<String> codeAnswer;
    /*
    @SerializedName("code_output")
    @Deprecated // this field is not used anymore, please use std_output_list
    private List<String> codeOutput;
     */

    @SerializedName("std_output_list")
    private List<String> stdOutputList;
    @SerializedName("expected_code_answer")
    private List<String> expectedCodeAnswer;
    /*
    @SerializedName("expected_code_output")
    @Deprecated // this field is not used anymore, please use expected_std_output_list
    private List<String> expectedCodeOutput;
     */

    @SerializedName("expected_std_output_list")
    private List<String> expectedStdOutputList;

    @SerializedName("correct_answer")
    private boolean correctAnswer;

    public List<String> getCodeAnswer() {
        return codeAnswer;
    }

    public void setCodeAnswer(List<String> codeAnswer) {
        this.codeAnswer = codeAnswer;
    }

    public List<String> getStdOutputList() {
        return stdOutputList;
    }

    public void setStdOutputList(List<String> stdOutputList) {
        this.stdOutputList = stdOutputList;
    }

    public List<String> getExpectedCodeAnswer() {
        return expectedCodeAnswer;
    }

    public void setExpectedCodeAnswer(List<String> expectedCodeAnswer) {
        this.expectedCodeAnswer = expectedCodeAnswer;
    }

    public List<String> getExpectedStdOutputList() {
        return expectedStdOutputList;
    }

    public void setExpectedStdOutputList(List<String> expectedStdOutputList) {
        this.expectedStdOutputList = expectedStdOutputList;
    }

    public boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String toString() {
        return "RunCodeResult{" +
            "codeAnswer=" + codeAnswer +
            ", stdOutputList=" + stdOutputList +
            ", expectedCodeAnswer=" + expectedCodeAnswer +
            ", expectedStdOutputList=" + expectedStdOutputList +
            ", correctAnswer=" + correctAnswer +
            '}';
    }
}