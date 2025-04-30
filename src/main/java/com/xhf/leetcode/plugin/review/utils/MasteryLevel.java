package com.xhf.leetcode.plugin.review.utils;

import com.intellij.ui.JBColor;
import com.xhf.leetcode.plugin.review.backend.algorithm.constant.FSRSRating;
import java.awt.Color;


// 掌握程度的枚举
public enum MasteryLevel {
    AGAIN(
        FSRSRating.AGAIN.getName(),
        0,
        new JBColor(new Color(246, 11, 11), new Color(252, 67, 39)) // 浅色主题：鲜红；深色主题：亮红
    ),
    HARD(
        FSRSRating.HARD.getName(),
        1,
        new JBColor(new Color(255, 165, 0), new Color(255, 140, 0)) // 浅色主题：亮橙；深色主题：深橙
    ),
    GOOD(
        FSRSRating.GOOD.getName(),
        2,
        new JBColor(new Color(12, 163, 217), new Color(132, 195, 252)) // 浅色主题：亮蓝；深色主题：宝蓝
    ),
    EASY(
        FSRSRating.EASY.getName(),
        3,
        new JBColor(new Color(47, 183, 47), new Color(53, 166, 123)) // 浅色主题：翠绿；深色主题：亮绿
    );

    private final String description; // 描述信息
    private final int level;          // 等级
    private final JBColor color;      // 颜色（支持浅色和深色主题）

    MasteryLevel(String description, int level, JBColor color) {
        this.description = description;
        this.level = level;
        this.color = color;
    }

    public static MasteryLevel getEnumByLevel(String levelStr) {
        for (MasteryLevel level : MasteryLevel.values()) {
            if (String.valueOf(level.getLevel()).equals(levelStr)) {
                return level;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }

    public JBColor getColor() {
        return color;
    }

    public int getLevel() {
        return this.level;
    }
}
