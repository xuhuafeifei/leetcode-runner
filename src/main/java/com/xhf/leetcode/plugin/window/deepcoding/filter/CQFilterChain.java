package com.xhf.leetcode.plugin.window.deepcoding.filter;

import com.xhf.leetcode.plugin.model.CompetitionQuestion;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CompetitionQuestion: 竞赛题目过滤链
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CQFilterChain implements FilterChain<CompetitionQuestion> {

    private final List<Filter<CompetitionQuestion, ?>> filters;

    public CQFilterChain() {
        filters = new ArrayList<>(4);
    }

    @Override
    public void removeFilter(Filter<CompetitionQuestion, ?> t) {
        filters.remove(t);
    }

    @Override
    public FilterChain<CompetitionQuestion> addFilter(Filter<CompetitionQuestion, ?> t) {
        filters.add(t);
        return this;
    }

    @Override
    public List<CompetitionQuestion> apply(List<CompetitionQuestion> targets) {
        // 获取可用过滤器
        List<Filter<CompetitionQuestion, ?>> usableFilters = filters.stream()
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
}
