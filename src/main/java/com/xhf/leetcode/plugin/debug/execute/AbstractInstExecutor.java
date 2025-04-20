package com.xhf.leetcode.plugin.debug.execute;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.setting.AppSettings;

public abstract class AbstractInstExecutor implements InstExecutor {

    /**
     * 额外执行指令. 只有在UI输出的情况下才会执行. 如果是console输出则不会执行额外指令.
     *
     * @param operations operations, 用于在UI输入模式下, 写入指令
     * @param cmdArgs cmdArgs, 用于在命令行模式下, 写入指令
     * @param readType readType, 用于区分UI输入模式和命令行模式
     */
    protected void doMoreInst(Operation[] operations, String[] cmdArgs, ReadType readType) {
        // 这么设置的原因可以参考JavaNInst
        if (readType == null) {
            readType = ReadType.getByName(AppSettings.getInstance().getReadTypeName());
        }
        if (AppSettings.getInstance().isUIOutput()) {
            if (readType == ReadType.UI_IN) {
                for (Operation operation : operations) {
                    InstSource.uiInstInput(Instruction.success(readType, operation, ""));
                }
            } else if (readType == ReadType.COMMAND_IN) {
                for (String cmd : cmdArgs) {
                    InstSource.userCmdInput(cmd);
                }
            }
        }
    }
}
