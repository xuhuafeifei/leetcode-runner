package com.xhf.leetcode.plugin.review.backend.service;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCard;
import com.xhf.leetcode.plugin.review.backend.card.QuestionCardReq;
import com.xhf.leetcode.plugin.review.backend.card.QuestionFront;
import com.xhf.leetcode.plugin.review.backend.model.QueryDim;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.Nullable;

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
    private final Project project;

    public RQServiceImpl(Project project) {
        this.project = project;
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
    public @Nullable ReviewQuestion getTopQuestion() {
        QuestionCard topCard = app.getTopCard();
        if (topCard == null) {
            return null;
        }
        return topCard.toReviewQuestion();
    }

    @Override
    public void rateQuestion(FSRSRating rating) {
        app.rateCard(rating.toInt());
    }

    @Override
    public void createQuestion(Question question, FSRSRating rating) {
        app.createCard(
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
                question.getTitle(),
                question.getDifficulty(),
                question.getStatus(),
                question.getAcRate(),
                rating.toInt()
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
    public List<ReviewQuestion> getAllQuestions() {
        List<QuestionCard> allCards = app.getAllCards();
        return allCards.stream()
                .map(QuestionCard::toReviewQuestion)
                .collect(Collectors.toList())
                ;

    }

    @Override
    public void deleteQuestion(Integer id) {
        app.deleteCardById(id);
    }

    @Override
    public void rateTopQuestion(FSRSRating rating) {
        app.rateCard(rating.toInt());
    }
}
