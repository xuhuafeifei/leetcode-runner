package com.xhf.leetcode.plugin.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LangIconInfo {

    private String langIconPath;
    private String text;

    public LangIconInfo(String langIconPath, String text) {
        this.langIconPath = langIconPath;
        this.text = text;
    }

    public String getLangIconPath() {
        return langIconPath;
    }

    public void setLangIconPath(String langIconPath) {
        this.langIconPath = langIconPath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
