package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppInstFactory implements InstructionFactory {
    // 单例
    private static CppInstFactory instance;
    private CppInstFactory() {
    }
    public static CppInstFactory getInstance() {
        if (instance == null) {
            synchronized (CppInstFactory.class) {
                if (instance == null) {
                    instance = new CppInstFactory();
                }
            }
        }
        return instance;
    }

    @Override
    public InstExecutor create(Instruction inst) {
        return null;
    }
}
