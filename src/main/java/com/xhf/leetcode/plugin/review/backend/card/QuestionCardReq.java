package com.xhf.leetcode.plugin.review.backend.card;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;

/**
 * @author 文艺倾年
 */
public class QuestionCardReq {
    private Integer id; // 卡片ID
    private QuestionFront front; // 卡片前面题目
    private String back; // 背部答案
    private FSRSRating fsrsRating; // 评分
    private Project project; // 项目

    public QuestionCardReq() {
    }

    public QuestionCardReq(Integer id, QuestionFront front, String back, FSRSRating fsrsRating, Project project) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.fsrsRating = fsrsRating;
        this.project = project;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public QuestionFront getFront() {
        return front;
    }

    public void setFront(QuestionFront front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public FSRSRating getFsrsRating() {
        return fsrsRating;
    }

    public void setFsrsRating(FSRSRating fsrsRating) {
        this.fsrsRating = fsrsRating;
    }

    public Project getProject() {
        return project;
    }
}
