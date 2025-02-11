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
    private String debugTestcase;
    private String frontendQuestionId;
    private String translatedTitle;

    @Deprecated // not used
    private String markdownPath;

    private String markdownContent;
    /**
     * 题目的slug(question的slug)
     */
    private String titleSlug;
    private String defaultTestcases;
    private String difficulty;
    /**
     * 详见{@link Topic}
     */
    private String topicId;
    private String solutionSlug;
    private DeepCodingInfo deepCodingInfo;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translateTitle) {
        this.translatedTitle = translateTitle;
    }

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

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setSolutionSlug(String solutionSlug) {
        this.solutionSlug = solutionSlug;
    }

    public String getSolutionSlug() {
        return solutionSlug;
    }

    public String getDebugTestcase() {
        return debugTestcase;
    }

    public void setDebugTestcase(String debugTestcase) {
        this.debugTestcase = debugTestcase;
    }

    @Override
    public String toString() {
        return "LeetcodeEditor{" +
                "questionId='" + questionId + '\'' +
                ", lang='" + lang + '\'' +
                ", exampleTestcases='" + exampleTestcases + '\'' +
                ", frontendQuestionId='" + frontendQuestionId + '\'' +
                ", translatedTitle='" + translatedTitle + '\'' +
                ", markdownPath='" + markdownPath + '\'' +
                ", markdownContent='" + markdownContent + '\'' +
                ", titleSlug='" + titleSlug + '\'' +
                ", defaultTestcases='" + defaultTestcases + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", topicId='" + topicId + '\'' +
                ", solutionSlug='" + solutionSlug + '\'' +
                '}';
    }

    public void setDeepCodingInfo(DeepCodingInfo deepCodingInfo) {
        this.deepCodingInfo = deepCodingInfo;
    }

    public DeepCodingInfo getDeepCodingInfo() {
        return deepCodingInfo;
    }
}
