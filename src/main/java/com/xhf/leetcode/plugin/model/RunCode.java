package com.xhf.leetcode.plugin.model;

import com.google.gson.annotations.SerializedName;

/**
 * the content of the code to be executed
 * <p>
 * leetcode platform needs this content to 'run code'
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RunCode {
    private String lang;
    private String question_id;
    /* code content */
    private String typed_code;
    /**
     * 运行代码的输入测试案例
     */
    private String data_input;
    @SerializedName("title_slug")
    private String titleSlug;

    private String frontendQuestionId;

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getQuestionId() {
        return question_id;
    }

    public void setQuestionId(String questionId) {
        this.question_id = questionId;
    }

    public String getTypeCode() {
        return typed_code;
    }

    public void setTypeCode(String typeCode) {
        this.typed_code = typeCode;
    }

    public String getDataInput() {
        return data_input;
    }

    public void setDataInput(String dataInput) {
        this.data_input = dataInput;
    }

    @Override
    public String toString() {
        return "RunCode{" +
                "lang='" + lang + '\'' +
                ", questionId='" + question_id + '\'' +
                ", typedCode='" + typed_code + '\'' +
                ", dataInput='" + data_input + '\'' +
                '}';
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public String getTitleSlug() {
        return this.titleSlug;
    }
}
