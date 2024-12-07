package com.xhf.leetcode.plugin.search.engine;

import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;
import java.util.List;

public interface SearchEngine<E> {
    void buildIndex(List<E> sources) throws Exception;

    List<E> search(String query) throws IOException, ParseException;

    void close() throws IOException;
}
