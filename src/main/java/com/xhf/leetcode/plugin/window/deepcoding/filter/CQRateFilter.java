package com.xhf.leetcode.plugin.window.deepcoding.filter;

import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.window.filter.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * CompetitionQuestion: 竞赛分数过滤器
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CQRateFilter implements Filter<CompetitionQuestion, String> {

    /**
     * 过滤项, 满足条件的题目会被保留
     */
    private final List<String> items;

    public CQRateFilter() {
        items = new ArrayList<>(4);
    }

    @Override
    public boolean doFilter(CompetitionQuestion q) {
        return contains(String.valueOf(q.getRating()));
    }

    @Override
    public Filter<CompetitionQuestion, String> addItem(String item) {
        items.add(item);
        return this;
    }

    /**
     * 比较分数区间
     * [low, high)
     */
    @Override
    public boolean contains(String item) {
        for (String scoreRange : items) {
            String[] split = scoreRange.split("-");
            int low = Integer.parseInt(split[0]), high = Integer.parseInt(split[1]);
            double sc = Double.parseDouble(item);
            if (sc >= low && sc < high) {
                return true;
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
