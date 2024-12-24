package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

public interface InstructionFactory {
    InstExecutor create(Instruction inst);
}
