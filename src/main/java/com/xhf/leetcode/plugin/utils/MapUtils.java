package com.xhf.leetcode.plugin.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class MapUtils {
    public static <K,V> Map<K, V> getMapFromList(List<K> a, List<V> b) {
        HashMap<K, V> map = new HashMap<>();
        if (a.size() != b.size()) {
            throw new RuntimeException("key list and value list size not equal");
        }
        // 同时遍历a, b
        for (int i = 0; i < a.size(); i++) {
            map.put(a.get(i), b.get(i));
        }
        return map;
    }
}
