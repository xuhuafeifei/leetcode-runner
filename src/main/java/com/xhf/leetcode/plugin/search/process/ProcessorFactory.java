package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.Context;
import com.xhf.leetcode.plugin.search.utils.CharType;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import java.lang.reflect.InvocationTargetException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ProcessorFactory {

    private static volatile ProcessorFactory instance = null;

    private ProcessorFactory() {
    }

    public static ProcessorFactory getInstance() {
        if (instance == null) {
            synchronized (ProcessorFactory.class) {
                if (instance == null) {
                    instance = new ProcessorFactory();
                }
            }
        }
        return instance;
    }

    public Processor createProcessor(Context context) {
        return createProcessor(judgeType(context));
    }

    public Processor createProcessor(char c) {
        return createProcessor(judgeType(c));
    }

    private CharType judgeType(char c) {
        if (CharacterHelper.isEnglishLetter(c)) {
            return CharType.EN;
        } else if (CharacterHelper.isCJKCharacter(c)) {
            return CharType.CN;
        } else if (CharacterHelper.isArabicNumber(c)) {
            return CharType.DIGIT;
        }
        return CharType.NON;
    }

    private CharType judgeType(Context context) {
        char c = context.getC();
        return judgeType(c);
    }

    public Processor createProcessor(CharType charType) {
        try {
            return charType.getProcessor().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
