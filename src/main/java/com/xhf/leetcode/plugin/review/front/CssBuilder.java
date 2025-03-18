package com.xhf.leetcode.plugin.review.front;

import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.Objects;

/**
 * 为不同QuestionCard构建不同的css
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CssBuilder {
    private String res;
    private String template = "<style>\n" +
            "    {{problem-info}}\n" +
            "    {{problem-info-p}}\n" +
            "\n" +
            "    .status {\n" +
            "        background-color: {{status-background}};\n" +
            "        color: rgba(25, 118, 210, {{opacity}});\n" +
            "        font-weight: bold;\n" +
            "    }\n" +
            "\n" +
            "    .problem-easy {\n" +
            "        background-color: #e8f5e9;\n" +
            "        color: rgba(46, 125, 50, {{opacity}});\n" +
            "        font-size: 1.1em;\n" +
            "        font-weight: bold;\n" +
            "    }\n" +
            "    \n" +
            "    .problem-medium {\n" +
            "        background-color: #fff3e0;\n" +
            "        color: rgba(240, 182, 35, {{opacity}});\n" +
            "        font-size: 1.1em;\n" +
            "        font-weight: bold;\n" +
            "    }\n" +
            "\n" +
            "    .problem-hard {\n" +
            "        background-color: #fff3e0;\n" +
            "        color: rgba(247, 6, 6, {{opacity}});\n" +
            "        font-size: 1.1em;\n" +
            "        font-weight: bold;\n" +
            "    }\n" +
            "\n" +
            "    .user-easy {\n" +
            "        background-color: #e8f5e9;\n" +
            "        color: rgba(46, 125, 50, {{opacity}});\n" +
            "    }\n" +
            "\n" +
            "    .user-medium {\n" +
            "        background-color: #fff3e0;\n" +
            "        color: rgba(245, 124, 0, {{opacity}});\n" +
            "    }\n" +
            "\n" +
            "    .user-hard {\n" +
            "        background-color: #f3e5f5;\n" +
            "        color: rgba(255, 0, 0, {{opacity}});\n" +
            "    }\n" +
            "\n" +
            "    .last-review {\n" +
            "        background-color: #f3e5f5;\n" +
            "        color: rgba(242, 55, 217, {{opacity}});\n" +
            "    }\n" +
            "\n" +
            "    .next-review {\n" +
            "        background-color: #e8eaf6;\n" +
            "        color: rgba(63, 81, 181, {{opacity}});\n" +
            "    }" +
            "\n" +
            "<div class=\"problem-info\">\n" +
            "    <p class=\"status\">{{status}}</p>\n" +
            "    <p class=\"{{problem-title}}\">{{title}}</p>\n" +
            "    <p class=\"{{user-rate}}\">{{rate}}</p>\n" +
            "    <p class=\"last-review\">{{last-modify}}</p>\n" +
            "    <p class=\"next-review\">{{next-review}}</p>\n" +
            "</div> ";

    public CssBuilder() {
        res = template;
    }

    /**
     * status: 当前题目复习状态[未开始/逾期/已完成]
     *                      [not start/over time/done]
     * @param status
     * @return
     */
    public CssBuilder addStatus(String status) {
        if (Objects.equals(status, "未开始") || Objects.equals(status, "not start")) {
            // 普通正常颜色
            res = res.replace("{{problem-info}}", "    .problem-info {\n" +
                    "        color: #333;\n" +
                    "        background-color: #f9f9f9;\n" +
                    "        border: 1px solid #ddd;\n" +
                    "        padding: 15px;\n" +
                    "        border-radius: 8px;\n" +
                    "        box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                    "    } ");
            res = res.replace("{{problem-info-p}}", "    .problem-info p {\n" +
                    "        margin: 8px 0;\n" +
                    "        padding: 5px 10px;\n" +
                    "        border-radius: 4px;\n" +
                    "    }");
            // 透明度为1
            res = res.replace("{{opacity}}", "1");
            // status正常背景色
            res = res.replace("{{status-background}}", "rgb(227, 242, 253)");
        } else if (Objects.equals(status, "逾期") || Objects.equals(status, "over time")) {
            // 普通正常颜色
            res = res.replace("{{problem-info}}", "    .problem-info {\n" +
                    "        color: #333;\n" +
                    "        background-color: #f9f9f9;\n" +
                    "        border: 1px solid #ddd;\n" +
                    "        padding: 15px;\n" +
                    "        border-radius: 8px;\n" +
                    "        box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                    "    }");
            // 增加下划线
            res = res.replace("{{problem-info-p}}", "    .problem-info p {\n" +
                    "        margin: 8px 0;\n" +
                    "        padding: 5px 10px;\n" +
                    "        border-radius: 4px;\n" +
                    "        text-decoration: underline;\n" +
                    "        text-decoration-style: wavy;\n" +
                    "        text-decoration-color: red;\n" +
                    "    } ");
            // 透明度为1
            res = res.replace("{{opacity}}", "1");
            // status背景色设置为红色
            res = res.replace("{{status-background}}", "rgb(239, 68, 68)");
        } else if (Objects.equals(status, "已完成") || Objects.equals(status, "done")) {
            // 灰色背景色
            res = res.replace("{{problem-info}}", "    .problem-info {\n" +
                    "        color: #888;\n" +
                    "        text-decoration: line-through;\n" +
                    "        text-decoration-color: #171717;\n" +
                    "        background-color: #c6c3c3;\n" +
                    "        border: 5px solid #ddd;\n" +
                    "        padding: 15px;\n" +
                    "        border-radius: 8px;\n" +
                    "        box-shadow: 0 8px 8px rgba(0,0,0,0.4);\n" +
                    "        position: relative;\n" +
                    "    }");
            res = res.replace("{{problem-info-p}}", "    .problem-info p {\n" +
                    "        margin: 8px 0;\n" +
                    "        padding: 5px 10px;\n" +
                    "        border-radius: 4px;\n" +
                    "    }");
            // 透明度为0.2
            res = res.replace("{{opacity}}", "0.2");
            // status正常背景色
            res = res.replace("{{status-background}}", "rgb(227, 242, 253)");
        }
        res = res.replace("{{status}}", status);
        return this;
    }

    public CssBuilder addTitle(String title, String difficulty) {
        /**
         * EASY
         * MEDIUM
         * HARD
         */
        if (Objects.equals(difficulty, "EASY")) {
            res = res.replace("{{problem-title}}", "problem-easy");
        } else if (Objects.equals(difficulty, "MEDIUM")) {
            res = res.replace("{{problem-title}}", "problem-medium");
        } else if (Objects.equals(difficulty, "HARD")) {
            res = res.replace("{{problem-title}}", "problem-hard");
        } else {
            res = res.replace("{{problem-title}}", "problem-easy");
            LogUtils.warn("error! difficulty not recognized by CssBuilder! difficulty = " + difficulty);
        }

        res = res.replace("{{title}}", title);
        return this;
    }

    /**
     * 很难/一般般/很轻松
     * very hard/average/very easy
     *
     * @param userRate
     * @return
     */
    public CssBuilder addUserRate(String userRate) {
        if (Objects.equals(userRate, "很难") || Objects.equals(userRate, "very hard")) {
            res = res.replace("{{user-rate}}", "user-hard");
        } else if (Objects.equals(userRate, "一般般") || Objects.equals(userRate, "average")) {
            res = res.replace("{{user-rate}}", "user-medium");
        } else if (Objects.equals(userRate, "很轻松") || Objects.equals(userRate, "very easy")) {
            res = res.replace("{{user-rate}}", "user-easy");
        } else {
            res = res.replace("{{user-rate}}", "user-easy");
            LogUtils.warn("error! userRate not recognized by CssBuilder! userRate = " + userRate);
        }
        res = res.replace("{{rate}}", userRate);

        return this;
    }

    public CssBuilder lastModify(String lastModify) {
        res = res.replace("{{last-modify}}", BundleUtils.i18nHelper("上一次做题时间: ", "Last time meet the question: ") + lastModify);
        return this;
    }

    public CssBuilder nextReview(String nextReview) {
        res = res.replace("{{next-review}}", BundleUtils.i18nHelper("下一次复习时间: ", "Next time review the question: ") + nextReview);
        return this;
    }

    public String toString() {
        return res;
    }

    public String build() {
        return res;
    }
}