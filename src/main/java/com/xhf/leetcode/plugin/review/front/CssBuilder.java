package com.xhf.leetcode.plugin.review.front;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.ColorUtil;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.ReviewStatus;
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
        if (Objects.equals(this.userRate, FSRSRating.AGAIN.getName()) || Objects.equals(this.userRate, FSRSRating.HARD.getName())) {
            template = template.replace("{{difficulty-color}}", "#F60B0BFF");
        } else if (Objects.equals(this.userRate, FSRSRating.GOOD.getName())) {
            template = template.replace("{{difficulty-color}}", "#FF8C00FF");
        } else if (Objects.equals(this.userRate, FSRSRating.EASY.getName())) {
            template = template.replace("{{difficulty-color}}", "#2FB72FFF");
        } else {
            LogUtils.warn("unknown userRate! this.userRate = " + this.userRate);
            template = template.replace("{{difficulty-color}}", "#2FB72FFF");
        }
        return template.replace("{{difficulty-content}}", this.userRate);
    }

    private String handleTitle(String template) {
        if (Objects.equals(this.difficulty, "EASY")) {
            template = template.replace("{{title-color}}", "#5CB85CFF");
        } else if (Objects.equals(this.difficulty, "MEDIUM")) {
            template = template.replace("{{title-color}}", "#F38618FF");
        } else if (Objects.equals(this.difficulty, "HARD")) {
            template = template.replace("{{title-color}}", "#EF3D3DFF");
        } else {
            LogUtils.warn("unknown difficulty! this.difficulty = " + this.difficulty);
            template = template.replace("{{title-css}}", "#5CB85CFF");
        }
        template = template.replace("{{title-content}}", this.title);
        return template;
    }

    private String createTemplate(String status) {
        String tmp;
        String theme = getTheme();
        if (Objects.equals(status, ReviewStatus.NOT_START.getCnName()) || Objects.equals(status, ReviewStatus.NOT_START.getEnName())) {
            tmp = Objects.equals(theme, "light") ? getCommon() : getCommonDark();
        } else if (Objects.equals(status, ReviewStatus.OVER_TIME.getCnName()) || Objects.equals(status, ReviewStatus.OVER_TIME.getEnName())) {
            tmp = Objects.equals(theme, "light") ? getOvertime() : getOvertimeDark();
        } else if (Objects.equals(status, ReviewStatus.DONE.getCnName()) || Objects.equals(status, ReviewStatus.DONE.getEnName())) {
            // tmp = Objects.equals(theme, "light") ? getDone() : getDoneDark();
            tmp = Objects.equals(theme, "light") ? getCommon() : getCommonDark(); // 废弃done状态
        } else {
            tmp = Objects.equals(theme, "light") ? getCommon() : getCommonDark();
        }
        return tmp.replace("{{status-content}}", status);
    }

    public String getCommon() {
        return "<html><body style='margin:0;padding:0;background:white;font-family:Arial;width:280px'>" +
            "<div style='padding:12px'>" +
            "    <p style='margin:6px 0;color:#1a73e8;font-weight:bold;font-size:14px'>{{status-content}}</p>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:15px;color:{{title-color}}'>{{title-content}}</p>" +
            "    <p style='margin:6px 0;font-size:13px;color:{{difficulty-color}}'>{{difficulty-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px;color:#5f6368'>{{lastModify-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px;color:#5f6368'>{{nextReview-content}}</p>" +
            "</div></body></html>";
    }

    public String getCommonDark() {
        return "<html><body style='margin:0;padding:0;background:#202124;font-family:Arial;width:280px;color:#e8eaed'>" +
            "<div style='padding:12px'>" +
            "    <p style='margin:6px 0;color:#8ab4f8;font-weight:bold;font-size:14px'>{{status-content}}</p>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:15px;color:{{title-color}}'>{{title-content}}</p>" +
            "    <p style='margin:6px 0;font-size:13px;color:{{difficulty-color}}'>{{difficulty-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px;color:#9aa0a6'>{{lastModify-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px;color:#9aa0a6'>{{nextReview-content}}</p>" +
            "</div></body></html>";
    }

    public String getOvertime() {
        return "<html><body style='margin:0;padding:0;background:#fce8e6;font-family:Arial;width:280px'>" +
            "<div style='padding:12px;color:#d93025'>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:14px'>{{status-content}}</p>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:15px'>{{title-content}}</p>" +
            "    <p style='margin:6px 0;font-size:13px'>{{difficulty-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px'>{{lastModify-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px'>{{nextReview-content}}</p>" +
            "</div></body></html>";
    }

    public String getOvertimeDark() {
        return "<html><body style='margin:0;padding:0;background:#3c1f1f;font-family:Arial;width:280px;color:#f28b82'>" +
            "<div style='padding:12px'>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:14px'>{{status-content}}</p>" +
            "    <p style='margin:6px 0;font-weight:bold;font-size:15px'>{{title-content}}</p>" +
            "    <p style='margin:6px 0;font-size:13px'>{{difficulty-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px'>{{lastModify-content}}</p>" +
            "    <p style='margin:6px 0;font-size:12px'>{{nextReview-content}}</p>" +
            "</div></body></html>";
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