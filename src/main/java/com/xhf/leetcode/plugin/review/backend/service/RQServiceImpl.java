package com.xhf.leetcode.plugin.review.backend.service;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;
import com.xhf.leetcode.plugin.review.backend.card.QuestionFront;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RQServiceImpl, 前端真正调用的接口
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RQServiceImpl implements ReviewQuestionService {


    private AlgorithmAPI api;
    private final Project project;

    private static RQServiceImpl instance;

    public RQServiceImpl(Project project) {
        this.project = project;
        this.api = new AlgorithmAPI(project);
    }

    @Override
    public @Nullable ReviewQuestion getTopQuestion() {
        QuestionCard topCard = api.getTopCard();
        if (topCard == null) {
            return null;
        }
        return topCard.toReviewQuestion();
    }

    @Override
    public void rateQuestion(FSRSRating rating) {
        api.rateCard(rating.toInt());
    }

    @Override
    public void createQuestion(Question question, FSRSRating rating) {
        api.createCard(
                toQuestionCardReq(question, rating)
        );
    }

    private QuestionCardReq toQuestionCardReq(Question question, FSRSRating rating) {
        int id = Question.getIdx(question, project);
        QuestionFront front = getQuestionFront(question, rating);
        return new QuestionCardReq(id, front, "", rating);
    }

    private QuestionFront getQuestionFront(Question question, FSRSRating rating) {
        return
                new QuestionFront(
                    question.getReviewTitle(),
                    question.getDifficulty(),
                    question.getStatus(),
                    question.getAcRate(),
                    rating.toInt(),
                    question.getReviewTitleCn()
                );
    }

    @Override
    public @NotNull List<ReviewQuestion>  getAllQuestions() {
        List<QuestionCard> allCards = api.getAllCards();
        return allCards.stream()
                .sorted(Comparator.comparingLong(QuestionCard::getNextReview))
                .map(QuestionCard::toReviewQuestion)
                .collect(Collectors.toList())
                ;

    }

    @Override
    public void deleteQuestion(Integer id) {
        api.deleteCardById(id);
    }

    @Override
    public void rateTopQuestion(FSRSRating rating) {
        api.rateCard(rating.toInt());
    }

    @Override
    public void updateBack(Integer id, String back) {
        api.updateCardBack(id, back);
    }
}
