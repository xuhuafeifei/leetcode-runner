package com.xhf.leetcode.plugin.search.lucence;

import com.vladsch.flexmark.util.sequence.builder.Seg;
import com.xhf.leetcode.plugin.search.Context;
import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.Segmentation;
import com.xhf.leetcode.plugin.search.SourceManager;
import com.xhf.leetcode.plugin.search.process.Processor;
import com.xhf.leetcode.plugin.search.process.ProcessorFactory;
import com.xhf.leetcode.plugin.search.utils.CharType;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public final class LCTokenizer extends Tokenizer {

    //词元文本属性
    private final CharTermAttribute termAtt;
    // 分词处理器, 获取下一个token
    private final Segmentation segmentation;

    public LCTokenizer(Reader reader) {
        super(reader);
        segmentation = new Segmentation(reader);
        termAtt = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        // 清空属性
        clearAttributes();
        // 获取下一个分词
        String token = segmentation.next();
        if (StringUtils.isNotBlank(token)) {
            termAtt.append(token);
            termAtt.setLength(token.length());
            return true;
        }
        return false;
    }
}
