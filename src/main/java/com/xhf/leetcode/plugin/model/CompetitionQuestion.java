package com.xhf.leetcode.plugin.model;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.service.QuestionService;

import java.util.List;

/**
 * 竞赛题目信息
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CompetitionQuestion implements DeepCodingQuestion {
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
    private String status;

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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Question toQuestion(Project project) {
        List<Question> totalQuestion = QuestionService.getInstance(project).getTotalQuestion(project);
        return totalQuestion.get(Integer.parseInt(getFid()) - 1);
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
        sb.append(" ").append("难度分: ").append((int) getRating()).append("   ");
        sb.append("[")
                .append(getFid())
                .append("]")
                .append(getTitleZH())
//                .append(AppSettings.getInstance().isZh() ? " " + getTitleZH() : " " + getTitle())
        ;
        sb.append("   【").append(getAlgorithm()).append("】");
        sb.append("   【").append(getContestID_zh()).append("】");
        return sb.toString();
    }
}
