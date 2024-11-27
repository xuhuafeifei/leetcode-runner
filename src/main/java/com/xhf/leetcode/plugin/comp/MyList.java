package com.xhf.leetcode.plugin.comp;

import com.intellij.ui.components.JBList;
import com.xhf.leetcode.plugin.bus.LCEvent;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.service.QuestionService;

import java.util.List;

public class MyList<T> extends JBList<T> {
    public void setListData(List<T> listData) {
        T[] data = (T[]) new Object[listData.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = listData.get(i);
        }
        this.setListData(data);
    }
}