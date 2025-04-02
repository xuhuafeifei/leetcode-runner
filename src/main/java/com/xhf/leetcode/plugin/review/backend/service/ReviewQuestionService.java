package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public interface ReviewQuestionService {
    /**
     * 获取用户当前需要复习的题目内容
     *
     * @return ReviewQuestion
     */
    @Nullable ReviewQuestion getTopQuestion();

    /**
     * 为问题评分
     * @param rating
     */
    void rateQuestion(FSRSRating rating);

    /**
     * 创建问题
     * @param question
     */
    void createQuestion(Question question, FSRSRating rating);

    /**
     * 获取所有question
     * @return
     */
    List<ReviewQuestion> getAllQuestions();

    void deleteQuestion(Integer id);

    void rateTopQuestion(FSRSRating rating);
}
