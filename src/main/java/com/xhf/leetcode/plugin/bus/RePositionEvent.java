package com.xhf.leetcode.plugin.bus;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RePositionEvent {
    private String langType;
    private VirtualFile file;
    private String frontendQuestionId;
    private String titleSlug;

    public RePositionEvent(String frontendQuestionId, String titleSlug, VirtualFile file, String langType) {
        this.frontendQuestionId = frontendQuestionId;
        this.titleSlug = titleSlug;
        this.file = file;
        this.langType = langType;
    }

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public VirtualFile getFile() {
        return file;
    }

    public void setFile(VirtualFile file) {
        this.file = file;
    }

    public String getLangType() {
        return langType;
    }

    public void setLangType(String langType) {
        this.langType = langType;
    }
}