package com.xhf.leetcode.plugin.model;

public class Question {
    private String frontendQuestionId;

    private Double acRate;

    /**
     * EASY
     * MEDIUM
     * HARD
     */
    private String difficulty;

    /**
     * AC
     * TRIED
     * NOT_STARTED
     */
    private String status;
    private int solutionNum;
    private String title;
    private String titleCn;
    private String titleSlug;

    private String translatedTitle;
    private String translatedContent;
    private String codeSnippets;

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public Double getAcRate() {
        return acRate;
    }

    public void setAcRate(Double acRate) {
        this.acRate = acRate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSolutionNum() {
        return solutionNum;
    }

    public void setSolutionNum(int solutionNum) {
        this.solutionNum = solutionNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleCn() {
        return titleCn;
    }

    public void setTitleCn(String titleCn) {
        this.titleCn = titleCn;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("      ");

        if ("AC".equals(getStatus())) {
            // sb.append("done ");
            sb.append("✔");
        } else if ("TRIED".equals(getStatus())) {
            sb.append("❓");
        } else {
            // sb.append("          ");
            sb.append("   ");
        }
        sb.append("[")
                .append(frontendQuestionId)
                .append("]")
                .append(getTitleCn())
        ;
        String res = sb.toString();
        return res;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public String getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(String codeSnippets) {
        this.codeSnippets = codeSnippets;
    }

    public String getFileName() {
        return  "[" + getFrontendQuestionId() + "]" + getTitleSlug();
    }
}
