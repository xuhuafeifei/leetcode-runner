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
        ReviewQuestionModel m1 = new ReviewQuestionModel();
        m1.setStatus("已完成");
        m1.setTitle("[1] 两数之和");
        m1.setDifficulty("简单");
        m1.setUserRate("一般般");
        m1.setLastModify("2024/03/11");
        m1.setNextReview("2024/03/18");
        res.add(m1);

        ReviewQuestionModel m2 = new ReviewQuestionModel();
        m2.setStatus("逾期");
        m2.setTitle("[617] N皇后");
        m2.setDifficulty("困难");
        m2.setUserRate("很难");
        m2.setLastModify("2023/02/13");
        m2.setLastModify("2023/02/18");
        res.add(m2);

        ReviewQuestionModel m3 = new ReviewQuestionModel();
        m3.setStatus("未开始");
        m3.setTitle("[889] 三树枝和");
        m3.setDifficulty("中等");
        m3.setUserRate("很轻松");
        m3.setLastModify("2011/04/11");
        m3.setNextReview("2011/04/22");
        res.add(m3);

        return res;
    }
}
