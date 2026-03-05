package com.xhf.leetcode.plugin.window.filter;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 题目过滤链
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class QFilterChain implements FilterChain<Question> {

    private final List<Filter<Question, ?>> filters;

    public QFilterChain() {
        filters = new ArrayList<>(4);
    }

    @Override
    public void removeFilter(Filter<Question, ?> t) {
        filters.remove(t);
    }

    @Override
    public FilterChain<Question> addFilter(Filter<Question, ?> t) {
        filters.add(t);
        return this;
    }

    @Override
    public List<Question> apply(List<Question> targets) {
        // 获取可用过滤器
        List<Filter<Question, ?>> usableFilters = filters.stream()
            .filter(Filter::usable)
            .collect(Collectors.toList());

        // 没有能进行过滤的过滤器
        if (usableFilters.isEmpty()) {
            LogUtils.info("没有可用过滤器, 不进行过滤...");
            return targets;
        }

        // 过滤
        LogUtils.info("执行过滤行为...");
        return targets.stream()
            .filter(e -> usableFilters.stream().allMatch(f -> f.doFilter(e)))
            .collect(Collectors.toList());
    }

    /**
     * 过滤数据的同时, 进行数据更新
     *
     * @param targets 需要过滤的目标数据
     * @param consumer 回调函数, 用过滤后的数据更新面板
     */
    public void apply(List<Question> targets, Consumer<List<Question>> consumer) {
        List<Question> apply = apply(targets);
        consumer.accept(apply);
    }
}
