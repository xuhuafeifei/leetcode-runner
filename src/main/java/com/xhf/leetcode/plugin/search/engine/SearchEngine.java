package com.xhf.leetcode.plugin.search.engine;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * 搜索引擎
 * @param <E> 索引对象类型
 */
public interface SearchEngine<E> {
    /**
     * 构建索引
     * @param sources 需要构建索引的数据源
     */
    void buildIndex(List<E> sources) throws Exception;

    /**
     * 通过query, 构建的索引中匹配出对应数据
     * @param query 查询字符串
     */
    List<E> search(String query) throws IOException, ParseException;

    /**
     * 关闭引擎
     */
    void close() throws IOException;
}
