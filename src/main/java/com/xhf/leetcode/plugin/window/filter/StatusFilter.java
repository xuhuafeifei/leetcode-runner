package com.xhf.leetcode.plugin.window.filter;

import com.xhf.leetcode.plugin.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态过滤器, 按照问题状态过滤题目
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class StatusFilter implements QFilter {
    /**
     * 过滤项, 满足条件的题目会被保留
     */
    private final List<String> items;

    public StatusFilter() {
        items = new ArrayList<>(4);
    }

    @Override
    public boolean doFilter(Question q) {
        return contains(q.getStatus());
    }

    @Override
    public Filter<Question, String> addItem(String item) {
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
