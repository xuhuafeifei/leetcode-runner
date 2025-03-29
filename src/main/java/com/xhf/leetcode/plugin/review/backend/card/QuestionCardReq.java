package com.xhf.leetcode.plugin.review.backend.card;

/**
 * @author 文艺倾年
 */
public class QuestionCardReq {
    private Integer id; // 卡片ID
    private QuestionFront front; // 卡片前面题目
    private String back; // 背部答案

    public QuestionCardReq() {
    }

    public QuestionCardReq(Integer id, QuestionFront front, String back) {
        this.id = id;
        this.front = front;
        this.back = back;
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
}
