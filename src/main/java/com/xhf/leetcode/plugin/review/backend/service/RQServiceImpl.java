package com.xhf.leetcode.plugin.review.backend.service;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;
import com.xhf.leetcode.plugin.review.backend.model.QueryDim;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RQServiceImpl, 前端真正调用的接口
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RQServiceImpl implements ReviewQuestionService {


    private final AlgorithmAPI app;

    public RQServiceImpl() {
        this.app = new AlgorithmAPI();
    }

    @Override
    @Deprecated
    public int getTotalCnt(QueryDim queryDim) {
        return 0;
    }

    @Override
    @Deprecated
    public List<ReviewQuestion> getTotalReviewQuestion(QueryDim queryDim) {
        return null;
    }

    @Override
    public ReviewQuestion getTopQuestion() {
        return app.getTopCard().toReviewQuestion();
    }

    @Override
    public void rateQuestion(FSRSRating rating) {
        app.rateCard(rating.toInt());
    }

    @Override
    public void createQuestion(Question question) {
        app.createCard(toQuestionCardReq(question));
    }

    private QuestionCardReq toQuestionCardReq(Question question) {
        return null;
    }

    @Override
    public List<ReviewQuestion> getAllQuestions() {
        List<QuestionCard> allCards = app.getAllCards();
        return allCards.stream()
                .map(QuestionCard::toReviewQuestion)
                .collect(Collectors.toList())
                ;

    }

    @Override
    public void deleteQuestion(Integer id) {

    }
}
