package com.xhf.leetcode.plugin.review.backend.algorithm.constant;

import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author 文艺倾年
 */
public enum FSRSRating {
    /**
     * 忘记；错误答案
     */
    AGAIN(0, BundleUtils.i18nHelper("需要重新复习", "need to code again")),
    /**
     * 回忆起来；经过一定困难才答出的正确答案
     */
    HARD(1, BundleUtils.i18nHelper("有点困难", "hard to code")),
    /**
     * 经过延迟答出的正确答案
     */
    GOOD(2, BundleUtils.i18nHelper("有思路, 能写出来", "know how to do it")),
    /**
     * 完美答案
     */
    EASY(3, BundleUtils.i18nHelper("小菜一碟", "a piece of cake"));


    private final Integer id;
    private final String name;
    /**
     * 构造函数
     *
     * @param id 评分的ID（0-3）
     * @param name name
     */
    FSRSRating(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String toName(Integer userRate) {
        for (FSRSRating rating : FSRSRating.values()) {
            if (rating.toInt().equals(userRate)) {
                return rating.getName();
            }
        }
        return BundleUtils.i18nHelper("未知", "unknown");
    }

    public static @NotNull FSRSRating getById(String levelStr) {
        for (FSRSRating rating : FSRSRating.values()) {
            if (String.valueOf(rating.id).equals(levelStr)) {
                return rating;
            }
        }
        LogUtils.warn("未知的评分：" + levelStr);
        return FSRSRating.EASY;
    }

    /**
     * 获取评分的整数值
     *
     * @return 评分的ID
     */
    public Integer toInt() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
