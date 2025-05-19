package com.xhf.leetcode.plugin.review.backend.model;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public interface ReviewQuestion {
    String getStatus();
    String getTitle();
    String getUserRate();
    String getLastModify();
    String getNextReview();
    String getDifficulty();
    String getUserNoteText();
    void   setBack(String newSolution);
    Integer getId();
}
