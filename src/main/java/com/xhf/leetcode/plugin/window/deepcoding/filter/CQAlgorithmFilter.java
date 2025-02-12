package com.xhf.leetcode.plugin.window.deepcoding.filter;

import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.window.filter.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 算法过滤器
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CQAlgorithmFilter implements Filter<CompetitionQuestion, String> {
    /**
     * 过滤项, 满足条件的题目会被保留
     */
    private final List<String> items;


    public CQAlgorithmFilter() {
        items = new ArrayList<>(2);
    }

    @Override
    public boolean doFilter(CompetitionQuestion competitionQuestion) {
        return contains(competitionQuestion.getAlgorithm());
    }

    @Override
    public Filter<CompetitionQuestion, String> addItem(String item) {
        items.add(item);
        return this;
    }

    @Override
    public boolean contains(String item) {
        for (String alg : items) {
            for (String s : item.split(",")) {
                if (alg.equals(s)) {
                    return true;
                }
            }
        }
        return false;
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
