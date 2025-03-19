package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.ColorUtil;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.awt.*;
import java.util.Objects;

/**
 * 为不同QuestionCard构建不同的css
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CssBuilder {
    private String status = "";
    private String title = "";
    private String difficulty = "";
    private String userRate = "";
    private String lastModify = "";
    private String nextReview = "";

    public CssBuilder() {
    }

    /**
     * status: 当前题目复习状态[未开始/逾期/已完成]
     *                      [not start/over time/done]
     * @param status
     * @return
     */
    public CssBuilder addStatus(String status) {
        if (status == null) {
            return this;
        }
        this.status = status;
        return this;
    }

    public CssBuilder addTitle(String title, String difficulty) {
        /**
         * EASY
         * MEDIUM
         * HARD
         */
        if (this.title != null) {
            this.title = title;
        }
        if (this.difficulty != null) {
            this.difficulty = difficulty;
        }
        return this;
    }

    /**
     * 很难/一般般/很轻松
     * very hard/average/very easy
     *
     * @param userRate 用户评价
     * @return this
     */
    public CssBuilder addUserRate(String userRate) {
        if (userRate == null) {
            return this;
        }
        this.userRate = userRate;
        return this;
    }

    public CssBuilder lastModify(String lastModify) {
        if (lastModify == null) {
            return this;
        }
        this.lastModify = lastModify;
        return this;
    }

    public CssBuilder nextReview(String nextReview) {
        if (nextReview == null) {
            return this;
        }
        this.nextReview = nextReview;
        return this;
    }

    public String toString() {
        return build();
    }

    public String build() {
        String template = createTemplate(status);
        template = handleTitle(template);
        template = handleUserRate(template);
        template = handleLastModify(template);
        template = handleNextReview(template);
        return template;
    }

    private String handleNextReview(String template) {
        return template.replace("{{nextReview-content}}", BundleUtils.i18nHelper("下一次复习时间: ", "next review time: ") + nextReview);
    }

    private String handleLastModify(String template) {
        return template.replace("{{lastModify-content}}", BundleUtils.i18nHelper("上一次做题时间: ", "last handle time: ") + lastModify);
    }

    private String handleUserRate(String template) {
        if (Objects.equals(this.userRate, "很难") || Objects.equals(this.userRate, "very hard")) {
            template = template.replace("{{difficulty-css}}", "difficulty-hard");
        } else if (Objects.equals(this.userRate, "一般般") || Objects.equals(this.userRate, "average")) {
            template = template.replace("{{difficulty-css}}", "difficulty-medium");
        } else if (Objects.equals(this.userRate, "很轻松") || Objects.equals(this.userRate, "very easy")) {
            template = template.replace("{{difficulty-css}}", "difficulty-easy");
        } else {
            LogUtils.warn("unknown userRate! this.userRate = " + this.userRate);
            template = template.replace("{{difficulty-css}}", "difficulty-easy");
        }
        return template.replace("{{difficulty-content}}", this.userRate);
    }

    private String handleTitle(String template) {
        if (Objects.equals(this.difficulty, "EASY")) {
            template = template.replace("{{title-css}}", "title-easy");
        } else if (Objects.equals(this.difficulty, "MEDIUM")) {
            template = template.replace("{{title-css}}", "title-medium");
        } else if (Objects.equals(this.difficulty, "HARD")) {
            template = template.replace("{{title-css}}", "title-hard");
        } else {
            LogUtils.warn("unknown difficulty! this.difficulty = " + this.difficulty);
            template = template.replace("{{title-css}}", "title-easy");
        }
        template = template.replace("{{title-content}}", this.title);
        return template;
    }

    private String createTemplate(String status) {
        String tmp;
        String theme = getTheme();
        if (Objects.equals(status, "未开始") || Objects.equals(status, "not start")) {
            tmp = Objects.equals(theme, "light") ? getCommon() : getCommonDark();
        } else if (Objects.equals(status, "逾期") || Objects.equals(status, "over time")) {
            tmp = Objects.equals(theme, "light") ? getOvertime() : getOvertimeDark();
        } else if (Objects.equals(status, "已完成") || Objects.equals(status, "done")) {
            tmp = Objects.equals(theme, "light") ? getDone() : getDoneDark();
        } else {
            tmp = Objects.equals(theme, "light") ? getCommon() : getCommonDark();
        }
        return tmp.replace("{{status-content}}", status);
    }

    public String getDone() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);\n" +
                "        background-color: #fff;\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #bcb6b6;\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "        text-decoration: line-through;\n" +
                "        color: #aaa;\n" +
                "    }\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    public String getDoneDark() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);\n" +
                "        background-color: #2b2b2b;\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #555;\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "        text-decoration: line-through;\n" +
                "        color: #777;\n" +
                "    }\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    private String getOvertime() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(255, 0, 0, 0.4); /* 红色阴影 */\n" +
                "        background-color: #fff5f5; /* 浅红背景 */\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #ff0000; /* 红色边框 */\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "        color: #ff0000; /* 纯红色字体 */\n" +
                "        font-weight: bold;\n" +
                "        text-decoration: underline wavy red; /* 波浪红色下划线，类似 IDEA 语法错误 */\n" +
                "    }\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    private String getOvertimeDark() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(255, 0, 0, 0.4); /* 红色阴影 */\n" +
                "        background-color: #2b2b2b; /* 深色背景 */\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #ff0000; /* 红色边框 */\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "        color: #ff0000; /* 纯红色字体 */\n" +
                "        font-weight: bold;\n" +
                "        text-decoration: underline wavy red; /* 波浪红色下划线，类似 IDEA 语法错误 */\n" +
                "    }\n" +
                "\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    private String getCommon() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);\n" +
                "        background-color: #fff;\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #bcb6b6;\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "    }\n" +
                "\n" +
                "    .status {\n" +
                "        color: rgb(71, 157, 255);\n" +
                "        font-weight: bold;\n" +
                "    }\n" +
                "\n" +
                "    .title {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(51, 51, 51);\n" +
                "    }\n" +
                "\n" +
                "    .title-easy {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(92, 184, 92);\n" +
                "    }\n" +
                "\n" +
                "    .title-medium {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(240, 173, 78);\n" +
                "    }\n" +
                "\n" +
                "    .title-medium {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(217, 83, 79);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(255, 165, 2);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-easy {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(92, 184, 92);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-medium {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(240, 173, 78);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-hard {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(217, 83, 79);\n" +
                "    }\n" +
                "\n" +
                "    .date {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(102, 102, 102);\n" +
                "    }\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    private String getCommonDark() {
        return "<style>\n" +
                "    .card {\n" +
                "        width: 230px;\n" +
                "        padding: 12px;\n" +
                "        border-radius: 8px;\n" +
                "        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);\n" +
                "        background-color: #2b2b2b;\n" +
                "        font-family: Arial, sans-serif;\n" +
                "        display: flex;\n" +
                "        flex-direction: column;\n" +
                "        border: 2px solid #555;\n" +
                "    }\n" +
                "\n" +
                "    .card p {\n" +
                "        margin: 10px 0;\n" +
                "    }\n" +
                "\n" +
                "    .status {\n" +
                "        color: rgb(97, 175, 239);\n" +
                "        font-weight: bold;\n" +
                "    }\n" +
                "\n" +
                "    .title {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(220, 220, 220);\n" +
                "    }\n" +
                "\n" +
                "    .title-easy {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(92, 184, 92);\n" +
                "    }\n" +
                "\n" +
                "    .title-medium {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(240, 173, 78);\n" +
                "    }\n" +
                "\n" +
                "    .title-hard {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(217, 83, 79);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty {\n" +
                "        color: rgb(255, 165, 2);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-easy {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(92, 184, 92);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-medium {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(240, 173, 78);\n" +
                "    }\n" +
                "\n" +
                "    .difficulty-hard {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(217, 83, 79);\n" +
                "    }\n" +
                "\n" +
                "    .date {\n" +
                "        font-weight: bold;\n" +
                "        color: rgb(232, 228, 228);\n" +
                "    }\n" +
                "\n" +
                "</style>\n" +
                "<div class=\"card\">\n" +
                "    <p class=\"status\">{{status-content}}</p>\n" +
                "    <p class=\"{{title-css}}\">{{title-content}}</p>\n" +
                "    <p class=\"{{difficulty-css}}\">{{difficulty-content}}</p>\n" +
                "    <p class=\"date\">{{lastModify-content}}</p>\n" +
                "    <p class=\"date\">{{nextReview-content}}</p>\n" +
                "</div>";
    }

    public String getTheme() {
        // 获取当前编辑器的颜色方案
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();

        // 直接获取默认背景颜色
        Color defaultBackground = globalScheme.getDefaultBackground();

        // 判断主题类型
        String vditorTheme;
        if (ColorUtil.isDark(defaultBackground)) {
            vditorTheme = "dark";
        } else {
            vditorTheme = "light";
        }
        return vditorTheme;
    }
}