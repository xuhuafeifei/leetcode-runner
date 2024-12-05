package com.xhf.leetcode.plugin.search;

import com.xhf.leetcode.plugin.search.process.Processor;
import com.xhf.leetcode.plugin.search.process.ProcessorFactory;
import com.xhf.leetcode.plugin.search.utils.CharType;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * 分词处理器, 获取下一个分词
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Segmentation {
    private final SourceManager sm;
    private final Iterator itr;
    private final ProcessorFactory pf;

    public Segmentation(Reader reader) {
        sm = new SourceManager();
        sm.setSource(reader);
        itr = sm.iterator();
        pf = ProcessorFactory.getInstance();
    }

    // 获取下一个分词
    public String next() throws IOException {
        if (! sm.tryLoad()) return null;
        // 创建上下文
        Context context = new Context(itr.next(), itr);
        // 判断当前处理的字符类型
        CharType charType = judgeType(context);
        // 过滤未识别的字符
        while (Objects.equals(charType.getType(), CharType.NON.getType())) {
            context.setC(itr.next());
            charType = judgeType(context);
        }
        // 获取处理器
        Processor processor = pf.createProcessor(charType);
        // 处理字符
        processor.doProcess(context);
        return context.getToken();
    }

    private CharType judgeType(Context context) {
        char c = context.getC();
        if (CharacterHelper.isEnglishLetter(c)) {
            return CharType.EN;
        } else if (CharacterHelper.isCJKCharacter(c)) {
            return CharType.CN;
        } else if (CharacterHelper.isArabicNumber(c)) {
            return CharType.DIGIT;
        }
        return CharType.NON;
    }
}
