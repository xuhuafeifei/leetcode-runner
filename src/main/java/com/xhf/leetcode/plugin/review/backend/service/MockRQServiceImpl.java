package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.review.backend.model.QueryDim;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟数据
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MockRQServiceImpl implements ReviewQuestionService {
    // 单例
    private static MockRQServiceImpl instance = null;
    private MockRQServiceImpl() {
    }
    public static MockRQServiceImpl getInstance() {
        if (instance == null) {
            instance = new MockRQServiceImpl();
        }
        return instance;
    }
    @Override
    public int getTotalCnt(QueryDim queryDim) {
        return 0;
    }

    @Override
    public List<ReviewQuestion> getTotalReviewQuestion(QueryDim queryDim) {
        ArrayList<ReviewQuestion> res = new ArrayList<>();
        res.add(new ReviewQuestionModel());
        res.add(new ReviewQuestionModel());
        return res;
    }
}
