package com.xhf.leetcode.plugin.debug.execute.python;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.InstructionFactory;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonInstFactory implements InstructionFactory {
    // 单例
    private static final PythonInstFactory instance = new PythonInstFactory();
    private PythonInstFactory() {
    }
    public static PythonInstFactory getInstance() {
        return instance;
    }

    public InstExecutor create(Instruction inst) {
        Operation operation = inst.getOperation();
        InstExecutor instExecutor = null;
        switch (operation) {
            case N:
                instExecutor = new PythonNInst();
                break;
            case R:
                instExecutor = new PythonRInst();
                break;
            case WATCH:
            case P:
                instExecutor = new PythonPInst();
                break;
            case W:
                instExecutor = new PythonWInst();
                break;
            case B:
                instExecutor = new PythonBInst();
                break;
            case SHOWB:
                instExecutor = new PythonSHOWBInst();
                break;
            case RB:
                instExecutor = new PythonRBInst();
                break;
            case RBA:
                instExecutor = new PythonRBAInst();
                break;
            case HELP:
                instExecutor = new PythonHELPInst();
                break;
            case STEP:
                instExecutor = new PythonSTEPInst();
            default:
                break;
        }
        return instExecutor;
    }
}
