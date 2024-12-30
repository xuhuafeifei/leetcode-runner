package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.utils.LangType;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static final String lineStart = "lc-start-line";
    public static final String lineEnd = "lc-end-line";

    /**
     * 目前只在class上方增加两行注释(下方offset暂不考虑)
     * @return
     */
    public static int getLineUpperOffset() {
        return 2;
    }

    /**
     * 处理代码片段, 形如下方所示. 此外, 注释会随着语言的变化而变化
     * // lc-start-line
     * // do not .....
     * ...code
     * // lc-end-line
     * @param code 代码片段
     * @return 处理后的代码片段
     */
    public static String handleCodeSnippets(String code, String langType) {
        String commentSymbol = LangType.getCommentSymbol(langType);
        return
                commentSymbol + "do not modify or remove anything between start-line and end-line" + "\n" +
                commentSymbol + lineStart + "\n" +
                code + "\n" +
                commentSymbol + lineEnd + "\n";
    }

    /**
     * 核心代码截取
     *
     * @param codeSnippets
     * @param langType
     * @return
     */
    public static String getCoreCodeSnippets(String codeSnippets, String langType) {
        StringBuilder sb = new StringBuilder();
        String[] lines = codeSnippets.split("\n");
        boolean inCoreCode = false;  // 标记是否在核心代码片段范围内

        for (String line : lines) {
            if (line.contains(lineStart)) {
                inCoreCode = true;  // 找到lineStart，开始截取核心代码
                continue;  // 跳过lineStart行
            }
            if (line.contains(lineEnd)) {
                break;  // 找到lineEnd，结束截取
            }
            if (inCoreCode) {
                sb.append(line).append("\n");  // 在核心代码范围内，追加到StringBuilder
            }
        }
        return sb.toString().trim();  // 返回去掉多余空行的核心代码
    }

    /**
     * 将curContent内部的核心代码替换为content
     * 核心代码指的是在lineStart和lineEnd之间的代码
     *
     * @param curContent 当前代码
     * @param content 需要替换的内容
     * @return 替换后的内容
     */
    public static String replaceCodeSnippets(@Nullable  String curContent, @NotNull String content) {
        // 如果当前内容为空, 或者不包含start 或 end, 直接返回被替换的内容
        if (StringUtils.isBlank(curContent) || !curContent.contains(lineStart) || !curContent.contains(lineEnd)) {
            return content;
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = curContent.split("\n");
        boolean inCoreCode = false;  // 标记是否在核心代码片段范围内

        for (String line : lines) {
            if (line.contains(lineStart)) {
                // 找到lineStart，开始截取核心代码
                inCoreCode = true;
                sb.append(line).append("\n");
                // 添加需要更换的内容
                sb.append(content);
                if (!content.endsWith("\n|\r")) {
                    sb.append("\n");
                }
                continue;
            }
            if (line.contains(lineEnd)) {
                sb.append(line).append("\n");
                continue;
            }
            // 只有不在核心代码区域内, 才会添加
            if (! inCoreCode) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();  // 返回去掉多余空行的核心代码
    }

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
