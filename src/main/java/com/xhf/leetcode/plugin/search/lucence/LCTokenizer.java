package com.xhf.leetcode.plugin.search.lucence;

import com.xhf.leetcode.plugin.search.Segmentation;
import com.xhf.leetcode.plugin.search.Token;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;
import java.io.Reader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public final class LCTokenizer extends Tokenizer {

    //词元文本属性
    private final TermAttribute termAtt;
    // 分词处理器, 获取下一个token
    private final Segmentation segmentation;

    public LCTokenizer(Reader reader) {
        super(reader);
        segmentation = new Segmentation(reader);
        termAtt = addAttribute(TermAttribute.class);
    }

    public LCTokenizer(Reader reader, int bufferSize) {
        super(reader);
        segmentation = new Segmentation(reader, bufferSize);
        termAtt = addAttribute(TermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        // 清空属性
        clearAttributes();
        // 获取下一个分词
        Token token = segmentation.next();
        if (token != null && StringUtils.isNotBlank(token.getToken())) {
            termAtt.setTermBuffer(token.getToken());
            termAtt.setTermLength(token.getLen());
            return true;
        }
        return false;
    }
}
