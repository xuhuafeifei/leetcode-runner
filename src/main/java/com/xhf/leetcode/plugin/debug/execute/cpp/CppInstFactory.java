package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.execute.python.*;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppInstFactory implements InstructionFactory {
    // 单例
    private static final CppInstFactory instance = new CppInstFactory();
    private CppInstFactory() {
    }
    public static CppInstFactory getInstance() {
        return instance;
    }

    public InstExecutor create(Instruction inst) {
        Operation operation = inst.getOperation();
        AbstractCppInstExecutor instExecutor = null;
        switch (operation) {
            case N:
                instExecutor = new CppNInst();
                break;
            case R:
                instExecutor = new CppRInst();
                break;
            case WATCH:
            case P:
                instExecutor = new CppPInst();
                break;
            case W:
                instExecutor = new CppWInst();
                break;
            case B:
                instExecutor = new CppBInst();
                break;
            case SHOWB:
                instExecutor = new CppSHOWBInst();
                break;
            case RB:
                instExecutor = new CppRBInst();
                break;
            case RBA:
                instExecutor = new CppRBAInst();
                break;
            case HELP:
                instExecutor = new CppHELPInst();
                break;
            case STEP:
                instExecutor = new CppSTEPInst();
            default:
                break;
        }
        return instExecutor;
    }
}
