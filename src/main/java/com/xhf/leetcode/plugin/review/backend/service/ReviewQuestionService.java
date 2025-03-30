package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.QueryDim;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public interface ReviewQuestionService {
    /**
     * 根据查询条件获取问题总数
     * @param queryDim 查询条件
     * @return 问题列表
     */
    @Deprecated
    int getTotalCnt(QueryDim queryDim);

    /**
     * 根据查询条件获取问题列表
     * @param queryDim 查询条件
     * @return 问题列表
     */
    @Deprecated
    List<ReviewQuestion> getTotalReviewQuestion(QueryDim queryDim);

    /**
     * 获取用户当前需要复习的题目内容
     *
     * @return ReviewQuestion
     */
    ReviewQuestion getTopQuestion();

    /**
     * 为问题评分
     * @param rating
     */
    void rateQuestion(FSRSRating rating);

    /**
     * 创建问题
     * @param question
     */
    void createQuestion(Question question);

    /**
     * 获取所有question
     * @return
     */
    List<ReviewQuestion> getAllQuestions();

    void deleteQuestion(Integer id);
}
