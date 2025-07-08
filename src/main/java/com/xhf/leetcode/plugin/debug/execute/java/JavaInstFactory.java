package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.execute.java.p.JavaPInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaInstFactory implements InstructionFactory {

    // 单例
    private static final JavaInstFactory instance = new JavaInstFactory();

    private JavaInstFactory() {
    }

    public static JavaInstFactory getInstance() {
        return instance;
    }

    @Override
    public InstExecutor create(Instruction inst) {
        Operation operation = inst.getOperation();
        InstExecutor instExecutor = null;
        switch (operation) {
            case N:
                instExecutor = new JavaNInst();
                break;
            case R:
                instExecutor = new JavaRInst();
                break;
            case WATCH:
            case P:
                instExecutor = new JavaPInst();
                break;
            case W:
                instExecutor = new JavaWInst();
                break;
            case B:
                instExecutor = new JavaBInst();
                break;
            case SHOWB:
                instExecutor = new JavaSHOWBInst();
                break;
            case RB:
                instExecutor = new JavaRBInst();
                break;
            case RBA:
                instExecutor = new JavaRBAInst();
                break;
            case HELP:
                instExecutor = new JavaHELPInst();
                break;
            case STEP:
                instExecutor = new JavaSTEPInst();
            default:
                break;
        }
        return instExecutor;
    }
}
