package com.xhf.leetcode.plugin.review.backend.card;

import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.ReviewStatus;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestion;
import com.xhf.leetcode.plugin.review.backend.model.ReviewQuestionModel;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 文艺倾年
 */
public class QuestionCard {

    /*
        id是Question在QuestionList中的index下标
     */

    private Integer id; // 卡片ID

    private final QuestionFront front; // 卡片前面题目
    private final String back; // 背部答案
    private Long nextReview;
    private Long created; // 创建时间

    /**
     * 构造函数，用于初始化卡片的所有传入参数
     * @param id 卡片的id，用于标识
     * @param front 卡片的正面文本
     * @param back 卡片的背面文本
     * @param created 卡片的创建时间
     */
    public QuestionCard(Integer id, QuestionFront front, String back, Long created) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.created = created;
    }


    /**
     * 获取唯一标识符
     * @return 唯一标识符
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置唯一标识符
     * @param id 唯一标识符
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取问题内容
     * @return 问题内容
     */
    public QuestionFront getFront() {
        return this.front;
    }

    public String getBack() {
        return this.back;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public Long getCreated() {
        return created;
    }

    /**
     * 设置创建时间
     * @param created 创建时间
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    public void setNextReview(Long nextReview) {
        this.nextReview = nextReview;
    }

    public Long getNextReview() {
        return this.nextReview;
    }

    public ReviewQuestion toReviewQuestion() {
        ReviewQuestionModel model = new ReviewQuestionModel();
        model.setId(this.getId());
        model.setUserSolution(this.getBack());
        model.setNextReview(this.handleNextReview());
        model.setLastModify(this.handleLastModify());
        model.setUserRate(FSRSRating.toName(this.getFront().getUserRate()));
        model.setStatus(this.handleStatus());
        model.setTitle(BundleUtils.i18nHelper(this.getFront().getTitleCn(), Objects.requireNonNullElse(this.getFront().getTitle(), "")));
        model.setDifficulty(this.getFront().getDifficulty());
        return model;
    }

    private String handleStatus() {
        // 判断当前题目状态, 是否逾期
        Long a = this.getNextReview();
        Long b = System.currentTimeMillis();
        // 判断时间差是否大于24h
        if (a != null && b - a > 24 * 60 * 60 * 1000) {
            return ReviewStatus.OVER_TIME.getName();
        }
        return ReviewStatus.NOT_START.getName();
    }

    private String handleLastModify() {
        Long time = this.getCreated();
        if (time != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(time));
        }
        return "";
    }

    private String handleNextReview() {
        Long time = this.getNextReview();
        // 把timestamp转换成日期格式, 处理成YYYY-MM-DD格式的字符串
        if (time != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(time));
        }
        return "";
    }

}
