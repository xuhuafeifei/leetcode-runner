package com.xhf.leetcode.plugin.model;

/**
 * store editor fill content, which will be used for run code
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LeetcodeEditor {
    private String questionId;
    private String lang;
    private String exampleTestcases;
    private String frontendQuestionId;

    @Deprecated // not used
    private String markdownPath;

    private String markdownContent;
    private String titleSlug;
    private String defaultTestcases;

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }

    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public String getDefaultTestcases() {
        return defaultTestcases;
    }

    public void setDefaultTestcases(String defaultTestcases) {
        this.defaultTestcases = defaultTestcases;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        lang = lang.toLowerCase();
        this.lang = lang;
    }

    public String getExampleTestcases() {
        return exampleTestcases;
    }

    public void setExampleTestcases(String exampleTestcases) {
        this.exampleTestcases = exampleTestcases;
    }

    public String getMarkdownPath() {
        return markdownPath;
    }

    public void setMarkdownPath(String markdownPath) {
        this.markdownPath = markdownPath;
    }

}
