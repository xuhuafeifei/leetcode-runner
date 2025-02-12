package com.xhf.leetcode.plugin.model;

/**
 * 竞赛题目信息
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CompetitionQuestion {
    private double Rating;
    private int ID;
    private String Title;
    private String TitleZH;
    private String TitleSlug;
    private String ContestSlug;
    private String ProblemIndex;
    private String ContestID_en;
    private String ContestID_zh;
    private String fid;
    private String difficulty;
    private String algorithm;

    public void setRating(double Rating) {
        this.Rating = Rating;
    }
    public double getRating() {
        return Rating;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public int getID() {
        return ID;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }
    public String getTitle() {
        return Title;
    }

    public void setTitleZH(String TitleZH) {
        this.TitleZH = TitleZH;
    }
    public String getTitleZH() {
        return TitleZH;
    }

    public void setTitleSlug(String TitleSlug) {
        this.TitleSlug = TitleSlug;
    }
    public String getTitleSlug() {
        return TitleSlug;
    }

    public String getContestSlug() {
        return ContestSlug;
    }

    public void setContestSlug(String contestSlug) {
        ContestSlug = contestSlug;
    }

    public String getProblemIndex() {
        return ProblemIndex;
    }

    public void setProblemIndex(String problemIndex) {
        ProblemIndex = problemIndex;
    }

    public String getContestID_en() {
        return ContestID_en;
    }

    public void setContestID_en(String contestID_en) {
        ContestID_en = contestID_en;
    }

    public String getContestID_zh() {
        return ContestID_zh;
    }

    public void setContestID_zh(String contestID_zh) {
        ContestID_zh = contestID_zh;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
