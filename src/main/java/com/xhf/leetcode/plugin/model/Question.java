package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.utils.LangType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Question {
    private String questionId;
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
    private List<TopicTag> topicTags;
    private String translatedTitle;
    private String translatedContent;
    private String codeSnippets;
    private String exampleTestcases;

    public List<TopicTag> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(List<TopicTag> topicTags) {
        this.topicTags = topicTags;
    }

    public void setExampleTestcases(String exampleTestcases) {
        this.exampleTestcases = exampleTestcases;
    }

    public String getExampleTestcases() {
        return exampleTestcases;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

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

    private static final Pattern pattern = Pattern.compile("\\[(.*?)]");
    public static String parseFrontendQuestionId(String fileName) {
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // 如果没有找到匹配的内容，返回 null 或者其他适当的值
    }

    public static String parseTitleSlug(String fileName) {
        int closingBracketIndex = fileName.indexOf(']');
        if (closingBracketIndex != -1 && closingBracketIndex < fileName.length() - 1) {
            fileName = fileName.substring(closingBracketIndex + 1);
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return null; // 如果没有找到匹配的内容，返回 null 或者其他适当的值
    }

    public static String parseLangType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return LangType.convertBySuffix(fileName.substring(dotIndex));
        }
        return null;
    }
}
