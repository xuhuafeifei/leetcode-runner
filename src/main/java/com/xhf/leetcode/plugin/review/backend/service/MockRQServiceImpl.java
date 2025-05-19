package com.xhf.leetcode.plugin.review.backend.service;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.model.QueryDim;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestionModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟数据
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MockRQServiceImpl implements ReviewQuestionService {

    private final Project project;
    private final List<ReviewQuestion> res = new ArrayList<>();
    private int idx = 0;
    // 单利
    private static MockRQServiceImpl instance = null;

    private MockRQServiceImpl(Project project) {
        this.project = project;
        ReviewQuestionModel m1 = new ReviewQuestionModel();
        m1.setStatus("已完成");
        m1.setTitle("[1] 两数之和");
        m1.setDifficulty("EASY");
        m1.setUserRate("一般般");
        m1.setLastModify("2024/03/11");
        m1.setNextReview("2024/03/18");
        m1.setUserSolution("<p>第一题</p>");
        res.add(m1);

        ReviewQuestionModel m2 = new ReviewQuestionModel();
        m2.setStatus("逾期");
        m2.setTitle("[617] N皇后");
        m2.setDifficulty("HARD");
        m2.setUserRate("很难");
        m2.setLastModify("2023/02/13");
        m2.setNextReview("2023/02/18");
        m2.setUserSolution("<p>第二题</p>");
        res.add(m2);

        ReviewQuestionModel m3 = new ReviewQuestionModel();
        m3.setStatus("未开始");
        m3.setTitle("[889] 三树枝和");
        m3.setDifficulty("MEDIUM");
        m3.setUserRate("很轻松");
        m3.setLastModify("2011/04/11");
        m3.setNextReview("2011/04/22");
        m3.setUserSolution("<p>第三题</p>");
        res.add(m3);
    }

    public static ReviewQuestionService getInstance(Project project) {
        if (instance == null) {
            instance = new MockRQServiceImpl(project);
        }
        return instance;
    }

    public int getTotalCnt(QueryDim queryDim) {
        return 0;
    }

    public List<ReviewQuestion> getTotalReviewQuestion(QueryDim queryDim) {
        ArrayList<ReviewQuestion> res = new ArrayList<>();
        ReviewQuestionModel m1 = new ReviewQuestionModel();
        m1.setStatus("已完成");
        m1.setTitle("[1] 两数之和");
        m1.setDifficulty("EASY");
        m1.setUserRate("一般般");
        m1.setLastModify("2024/03/11");
        m1.setNextReview("2024/03/18");
        m1.setUserSolution("<p>第一题</p>");
        res.add(m1);

        ReviewQuestionModel m2 = new ReviewQuestionModel();
        m2.setStatus("逾期");
        m2.setTitle("[617] N皇后");
        m2.setDifficulty("HARD");
        m2.setUserRate("很难");
        m2.setLastModify("2023/02/13");
        m2.setNextReview("2023/02/18");
        m2.setUserSolution("<p>第二题</p>");
        res.add(m2);

        ReviewQuestionModel m3 = new ReviewQuestionModel();
        m3.setStatus("未开始");
        m3.setTitle("[889] 三树枝和");
        m3.setDifficulty("MEDIUM");
        m3.setUserRate("很轻松");
        m3.setLastModify("2011/04/11");
        m3.setNextReview("2011/04/22");
        m3.setUserSolution("<p>第三题</p>");
        res.add(m3);

        return res;
    }

    @Override
    public ReviewQuestion getTopQuestion() {
        ReviewQuestion reviewQuestion = res.get(idx);
        this.idx = (idx + 1) % res.size();
        return reviewQuestion;
    }

    @Override
    public void rateQuestion(FSRSRating rating, String back) {

    }

    @Override
    public void rateQuestionByCardId(Integer cardId, FSRSRating rating, String back) {

    }

    @Override
    public void createQuestion(Question question, FSRSRating rating, String back) {

    }

    @Override
    public @NotNull List<ReviewQuestion> getAllQuestions() {
        return this.res;
    }

    @Override
    public void deleteQuestion(Integer id) {

    }

    @Override
    public void rateTopQuestion(FSRSRating rating) {

    }

    @Override
    public void updateBack(Integer id, String back) {

    }
}
