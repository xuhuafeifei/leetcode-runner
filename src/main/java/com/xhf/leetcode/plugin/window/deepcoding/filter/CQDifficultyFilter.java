package com.xhf.leetcode.plugin.window.deepcoding.filter;

import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.window.filter.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * CompetitionQuestion: 难度过滤器, 按照难度过滤题目
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CQDifficultyFilter implements Filter<CompetitionQuestion, String> {
    /**
     * 过滤项, 满足条件的题目会被保留
     */
    private final List<String> items;

    public CQDifficultyFilter() {
        items = new ArrayList<>(4);
    }

    @Override
    public boolean doFilter(CompetitionQuestion q) {
        return contains(q.getDifficulty());
    }

    @Override
    public Filter<CompetitionQuestion, String> addItem(String item) {
        items.add(item);
        return this;
    }

    @Override
    public boolean contains(String item) {
        return items.contains(item);
    }

    @Override
    public boolean removeItem(String item) {
        return items.remove(item);
    }

    @Override
    public void removeAllItems() {
        items.clear();
    }

    @Override
    public int itemCount() {
        return items.size();
    }

    @Override
    public boolean usable() {
        return itemCount() != 0;
    }
}
