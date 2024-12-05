package com.xhf.leetcode.plugin.search.process;

import com.xhf.leetcode.plugin.search.utils.CharType;

import java.lang.reflect.InvocationTargetException;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ProcessorFactory {

    private static volatile ProcessorFactory instance = null;
    private ProcessorFactory() {}
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

    public Processor createProcessor(CharType charType) {
        try {
            return charType.getProcessor().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
