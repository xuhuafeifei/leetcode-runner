package com.xhf.leetcode.plugin.window.filter;

import com.xhf.leetcode.plugin.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * 类别过滤器, 按照问题类别过滤题目
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CategoryFilter implements QFilter {
    /**
     * 过滤项, 满足任意条件的题目会被保留
     */
    private final List<String> items;
    /**
     * md, 通过类别过滤javascript的题目, 需要特别判断. 内部存储的时候是js题目的fid
     */
    private final String[] jsFilterItems = {"2618","2619","2620","2621","2622","2623","2624","2625","2626","2627","2628","2629","2630","2631","2632","2633","2634","2635","2636","2637","2648","2649","2650","2665","2666","2667","2675","2676","2677","2690","2691","2692","2693","2694","2695","2700","2703","2704","2705","2715","2721","2722","2723","2724","2725","2726","2727","2754","2755","2756","2757","2758","2759","2774","2775","2776","2777","2794","2795","2796","2797","2803","2804","2805","2821","2822","2823"};

    public CategoryFilter() {
        items = new ArrayList<>(4);
    }

    @Override
    public boolean doFilter(Question q) {
        if (q == null) {
            return false;
        }
        // 包含algorithm 过滤条件, 全部放行(这个就偷懒判断了, 毕竟广义来讲, 所有coding都算算法)
        if (contains("algorithms")) {
            return true;
        }
        if (q.getTopicTags() == null) {
            return false;
        }
        // 包含过滤条件包含javascript过滤条件(这个需要特判)
        if (contains("javascript")) {
            boolean flag = jsCategoryCheck(q.getFrontendQuestionId());
            if (flag) {
                return true;
            }
        }
        return q.getTopicTags().stream().anyMatch(tag -> contains(tag.getSlug()));
    }

    private boolean jsCategoryCheck(String frontendQuestionId) {
        for (String fid : jsFilterItems) {
            if (fid.equals(frontendQuestionId)) {
                return true;
            }
        }
        return false;
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
