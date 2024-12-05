package com.xhf.leetcode.plugin.search.lucence;

import org.apache.lucene.analysis.Analyzer;

import java.io.Reader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        LCTokenizer lcTokenizer = new LCTokenizer(reader);
        return new TokenStreamComponents(lcTokenizer, lcTokenizer);
    }
}
