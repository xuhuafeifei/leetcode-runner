package com.xhf.leetcode.plugin.window.filter;

import com.xhf.leetcode.plugin.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签过滤器, 按照问题标签过滤
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TagsFilter implements QFilter {
    /**
     * 过滤项, 满足条件的题目会被保留
     */
    private final List<String> items;


    public TagsFilter() {
        items = new ArrayList<>(2);
    }

    @Override
    public boolean doFilter(Question q) {
        if (q == null || q.getTopicTags() == null || q.getTopicTags().isEmpty()) {
            return false;
        }
        return q.getTopicTags().stream().anyMatch(tag -> contains(tag.getSlug()));
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
