package com.xhf.leetcode.plugin.search.lucence;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LCTokenizer(reader);
    }
}
