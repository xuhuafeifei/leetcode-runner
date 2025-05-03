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


    private final AlgorithmAPI api;
    private final Project project;

    private static RQServiceImpl instance;

    private RQServiceImpl(Project project) {
        this.project = project;
        this.api = AlgorithmAPI.getInstance(project);
    }

    public static RQServiceImpl getInstance(Project project) {
        if (instance == null) {
            instance = new RQServiceImpl(project);
        }
        return instance;
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
        int id = getId(question);
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

    int getId(Question question) {
        String fid = question.getFrontendQuestionId();
        if (fid != null) {
            try {
                return Integer.parseInt(fid) - 1;
            } catch (Exception e) {
                LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            }
        }

        String titleSlug = question.getTitleSlug();
        List<Question> totalQuestion = QuestionService.getInstance(project).getTotalQuestion(project);

        // 遍历所有元素, 找到匹配的titleSlug, 然后返回index
        for (int i = 0; i < totalQuestion.size(); i++) {
            Question q = totalQuestion.get(i);
            if (q.getTitleSlug().equals(titleSlug)) {
                return i;
            }
        }

        // 抛出异常
        throw new IllegalArgumentException("question not found");
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
