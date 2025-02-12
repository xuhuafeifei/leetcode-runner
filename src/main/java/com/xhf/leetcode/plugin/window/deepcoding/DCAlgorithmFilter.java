package com.xhf.leetcode.plugin.window.deepcoding;

import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.QFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务于deep coding模式下, 搜索使用的filter, 其过滤时用于判断传入的数据
 * 是所有fid 的集合, 并且通过,连接. 该类不具备通用性
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
class DCAlgorithmFilter implements QFilter {
    private final List<String> items = new ArrayList<>(3);

    @Override
    public boolean doFilter(Question question) {
        return contains(question.getFrontendQuestionId());
    }

    @Override
    public Filter<Question, String> addItem(String item) {
        items.add(item);
        return this;
    }

    /**
     * item是所有题目fid的集合, 并通过,连接
     * @param item item
     * @return boolean
     */
    @Override
    public boolean contains(String item) {
        // 判断是否是hot 150
        for (String it : items) {
            String[] split = it.split(",");
            for (String fid : split) {
                if (fid.equals(item)) {
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
