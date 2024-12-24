package com.xhf.leetcode.plugin.debug.execute.java;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.java.AbstractJavaInstExecutor;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaHELPInst extends AbstractJavaInstExecutor {
    private static final String HELP_INFO =
            "帮助文档格式: 命令名词 [命令输入形式] 命令作用\n" +
                    "\n" +
                    "N命令 [n | n 数字 | n 数字]  step into单步执行\n" +
                    "\n" +
                    "R命令 [r] 运行代码, 直到下一个断点\n" +
                    "\n" +
                    "P命令 [p] 打印本地变量\n" +
                    "\n" +
                    "B命令 [b 数字] 在指定行打上断点\n" +
                    "\n" +
                    "SHOWB命令 [show b | s b | sb] 显示所有断点\n" +
                    "\n" +
                    "RB命令 [remove b 数字|r b 数字|rb 数字|remove b数字|r b数字|rb数字] 移除指定行断点\n" +
                    "\n" +
                    "RBA命令 [remove all|ra|r a]移除所有断点\n" +
                    "\n" +
                    "W命令 [w] 查看当前所在位置\n" +
                    "\n" +
                    "STEP命令 [step out | step over] 功能和idea的debug对应按钮功能一致\n"
            ;
    @Override
    public ExecuteResult doExecute(Instruction inst, Context context) {
        ExecuteResult success = ExecuteResult.success(inst.getOperation(), HELP_INFO);
        DebugUtils.fillExecuteResultByLocation(success, context.getLocation());
        return success;
    }
}
