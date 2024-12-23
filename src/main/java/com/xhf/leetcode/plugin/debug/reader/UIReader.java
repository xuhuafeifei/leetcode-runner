package com.xhf.leetcode.plugin.debug.reader;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;

/**
 * 从UI读取输入
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class UIReader extends AbstractInstReader{
    public UIReader(Project project) {
        super(project, ReadType.UI_IN);
    }

    @Override
    public Instruction readInst() {
        return InstSource.consumeInst();
    }
}
