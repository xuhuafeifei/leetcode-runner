package com.xhf.leetcode.plugin.search.lucence;

import com.xhf.leetcode.plugin.search.SourceManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCAnalyzer extends Analyzer {
    private final int bufferSize;

    public LCAnalyzer(int bufferSize) {
        if (bufferSize < SourceManager.MIN_BUFFER_SIZE) {
            throw new IllegalArgumentException("bufferSize is too small!");
        }
        if (bufferSize > SourceManager.DEFAULT_BUFFER_SIZE) {
            throw new IllegalArgumentException("bufferSize is too large!");
        }
        this.bufferSize = bufferSize;
    }

    public LCAnalyzer() {
        this.bufferSize = SourceManager.DEFAULT_BUFFER_SIZE;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LCTokenizer(reader, bufferSize);
    }
}
