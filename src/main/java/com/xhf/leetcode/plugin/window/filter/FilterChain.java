package com.xhf.leetcode.plugin.window.filter;

import com.xhf.leetcode.plugin.model.Question;

import java.util.List;

/**
 * 执行过滤链操作
 * @param <T> 过滤的对象类型
 */
public interface FilterChain<T> {
    void removeFilter(Filter<T, ?> t);
    FilterChain<Question> addFilter(Filter<T, ?> t);
    List<Question> apply(List<T> targets);
}
