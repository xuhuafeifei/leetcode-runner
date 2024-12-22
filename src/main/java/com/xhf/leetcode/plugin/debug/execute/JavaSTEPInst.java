package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.Location;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;

/**
 * 更多信息可参考{@link JavaNInst}
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaSTEPInst implements InstExecutor {
    @Override
    public ExecuteResult execute(Instrument inst, Context context) {
        // 这里设置原因和N Inst一致
        if (AppSettings.getInstance().isUIOutput()) {
            InstSource.uiInstInput(Instrument.success(ReadType.UI_IN, Operation.W, ""));
            Location location = context.getLocation();
            String info = DebugUtils.buildCurrentLineInfoByLocation(location);
            DebugUtils.simpleDebug("N 指令执行时, 处于 " + info, context.getProject());
        }
        // 停止上一轮的单步请求
        StepRequest stepRequest = context.getStepRequest();
        if (stepRequest != null) {
            stepRequest.disable();
        }
        // 设置单步请求
        switch (inst.getParam()) {
            case "over":
                stepRequest = context.getErm().createStepRequest(context.getThread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
                break;
            case "out":
                stepRequest = context.getErm().createStepRequest(context.getThread(), StepRequest.STEP_LINE, StepRequest.STEP_OUT);
                break;
            default:
                return ExecuteResult.fail(inst.getOperation(), "未知的STEP指令参数: " + inst.getParam());
        }
        stepRequest.addCountFilter(1);
        stepRequest.enable();
        context.setStepRequest(stepRequest);
        return ExecuteResult.success(inst.getOperation());
    }
}
