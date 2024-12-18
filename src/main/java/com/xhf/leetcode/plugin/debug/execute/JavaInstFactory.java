package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaInstFactory {
    // 单例
    private static JavaInstFactory instance = new JavaInstFactory();
    private JavaInstFactory() {
    }
    public static JavaInstFactory getInstance() {
        return instance;
    }

    public InstExecutor create(Instrument inst) {
        Operation operation = inst.getOperation();
        InstExecutor instExecutor = null;
        switch (operation) {
            case N:
                instExecutor = new JavaNInst();
                break;
            case R:
                instExecutor = new JavaRInst();
                break;
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
            default:
                break;
        }
        return instExecutor;
    }
}
