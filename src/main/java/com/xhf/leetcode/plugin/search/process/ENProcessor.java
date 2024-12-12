package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;
import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.lucene.analysis.CharacterUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ENProcessor implements Processor {
    private final StringBuilder sb;

    public ENProcessor() {
        sb = new StringBuilder();
    }
    @Override
    public void doProcess(Context context) {
        sb.delete(0, sb.length());

        char c = context.getC();
        int len = 1;
        Iterator itr = context.getIterator();
        sb.append(c);

        while (itr.hasNext()) {
            char next = itr.peekNext();
            if (CharacterHelper.isEnglishLetter(next)) {
                sb.append(next);
                itr.next();
                len += 1;
            }else {
                break;
            }
        }

        context.addToken(sb.toString(), len);
    }
}
