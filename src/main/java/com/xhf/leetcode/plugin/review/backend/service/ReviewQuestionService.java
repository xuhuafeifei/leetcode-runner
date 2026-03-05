package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     */
    void rateQuestion(FSRSRating rating, String back);

    /**
     * 为问题评分
     */
    void rateQuestionByCardId(Integer cardId, FSRSRating rating, String back);

    /**
     * 创建问题
     */
    void createQuestion(Question question, FSRSRating rating, String back);

    /**
     * 获取所有question
     */
    @NotNull List<ReviewQuestion> getAllQuestions();

    void deleteQuestion(Integer id);

    void rateTopQuestion(FSRSRating rating);

    void updateBack(Integer id, String back);
}
