package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.Location;
import com.sun.jdi.event.EventSet;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaNInst implements InstExecutor{
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        /*
         * 如果是UI输出, 则生产打印指令, 并且不执行任何操作
         * 之所以要生成打印指令, 是因为N指令执行无法真正让代码断点运行到下一行. 因此高亮显示的代码
         * 是当前行, 而不是下一行.
         * 为了解决上述问题, 通过延迟高亮显示的方式解决. 每次执行N指令, 都会产生一条W指令. 当N指令执行完成
         * 后, 在下一轮循环开始会执行W指令. 而下一轮循环开始时, 已经执行到下一行代码, 因此间接实现高亮下一行代码
         *
         * eg:
         * 假设程序正在执行line code 13:
         *
         * step0: 输入N指令
         * step1 :读取N指令
         * step2: 执行N指令, 产生W指令
         * step3: 检测到N指令, 终止读取, 回复所有线程, debug运行下一行代码 -> line code 14
         * step4: 读取到W指令
         * step5: 执行W指令
         * step6: 高亮当前代码 -> line code 14
         *
         * 至此, 实现了在第13行执行N指令, 高亮显示下一行代码, 也就是14行
         */
        if (AppSettings.getInstance().isUIOutput()) {
            InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.W, ""));
            // 输出一下当前所在行号, 不然UI显示的时候遇到系统代码, 啥也看不到, 就很操蛋
            Location location = context.getLocation();
            String info = DebugUtils.buildCurrentLineInfoByLocation(location);
            DebugUtils.simpleDebug("N 指令执行时, 处于 " + info, context.getProject());
        }
        return ExecuteResult.success(inst.getOperation());
    }
}
