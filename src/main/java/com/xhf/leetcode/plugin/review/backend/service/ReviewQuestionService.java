package com.xhf.leetcode.plugin.review.backend.service;

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
    int getTotalCnt(QueryDim queryDim);

    /**
     * 根据查询条件获取问题列表
     * @param queryDim 查询条件
     * @return 问题列表
     */
    List<ReviewQuestion> getTotalReviewQuestion(QueryDim queryDim);
}
