package com.xhf.leetcode.plugin.debug.output;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.comp.MyList;
import com.xhf.leetcode.plugin.debug.execute.Context;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.JavaWInst;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.params.Operation;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.DataKeys;
import com.xhf.leetcode.plugin.window.LCConsoleWindowFactory;
import org.apache.commons.lang.StringUtils;

/**
 * UIOutput, 负责在UI界面输出信息. 该类主要负责处理通过UI进行debug调试的输出显示
 * <p>
 * 但为了更好的拓展项目功能, 很多指令均进行适配. 该方法支持从命令行读取的指令执行的结果输出
 * 适配的方法均用@Adapt注解标注
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class UIOutput extends AbstractOutput{
    private MyList<String> variables;
    private final AppSettings appInstance;

    public UIOutput(Project project) {
        super(project);
        appInstance = AppSettings.getInstance();
        ApplicationManager.getApplication().invokeLater(() -> {
            variables = LCConsoleWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_DEBUG_VARIABLE_LIST);
        });
    }

    @Override
    public void output(ExecuteResult r) {
        Operation op = r.getOperation();
        // 记录日志
        DebugUtils.simpleDebug(r.toString(), project);

        switch(op) {
            case R:
                doR(r);
                break;
            case N:
                // doAfter(r); N指令执行doAfter并不能高亮显示下一行数据. 因为此时获取的addLine是上一行, 而不是下一行
                break;
            case P:
                doP(r);
                break;
            case W:
                doW(r);
                break;
            case B:
                doB_new(r);
                break;
            case RB:
                doRB(r);
                break;
            case SHOWB:
                doSHOWB(r);
                break;
            case RBA:
                doRBA(r);
                break;
            case HELP:
                doHELP(r);
                break;
            default:
                DebugUtils.simpleDebug("指令" + op.getName() + "不被UIOutput处理", project);
        }
    }

    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doHELP(ExecuteResult r) {
        if (r.isSuccess()) {
            if (appInstance.isCommandReader() && r.isHasResult()) {
                String result = r.getResult();
                variables.setListData(result.split("\r|\n"));
            }
        }
    }

    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doSHOWB(ExecuteResult r) {
        if (r.isSuccess()) {
            if (appInstance.isCommandReader()) {
                // double-check. 按理来说, show b 一定会有输出. 但这里额外做一层保险
                if (r.isHasResult()) {
                    String result = r.getResult();
                    String[] split = result.split("\r|\n");
                    variables.setListData(split);
                }
            }
        }
    }

    private void doR(ExecuteResult r) {
         DebugUtils.removeHighlightLine(project);
    }

    private void doAfter(ExecuteResult r) {
        // 获取当前执行的行号
        Context ctx = r.getContext();
        ExecuteResult execute =
                new JavaWInst().execute(Instrument.success(
                    ReadType.UI_IN, Operation.W, ""
                ), ctx);
        int lineNumber = execute.getAddLine();

        highlightLineWithCheck(lineNumber, execute.getClassName());
    }

    public void highlightLineWithCheck(int lineNumber, String curClassName) {
        // 如果当前执行的类不是Solution, 则不进行高亮
        if (! "Solution".equals(curClassName)) {
            DebugUtils.removeHighlightLine(project);
            return;
        }
        DebugUtils.simpleDebug("当前执行行号: " + lineNumber + " 设置高亮...", project);
        DebugUtils.highlightLine(project, lineNumber);
    }

    /**
     * 如果是命令行输入, 则需要将结果输出到UI界面
     * 如果是UI输入, 则高亮显示
     * @param r
     */
    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doW(ExecuteResult r) {
        if (r.isSuccess()) {
            if (appInstance.isUIReader()) {
                highlightLineWithCheck(r.getAddLine(), r.getClassName());
            } else if (appInstance.isCommandReader() && r.isHasResult()) {
                String[] split = r.getResult().split("\n|\r");
                variables.setListData(split);
            }
        }
    }

    /**
     * 清除所有断点
     * @param r r
     */
    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doRBA(ExecuteResult r) {
        if (r.isSuccess()) {
            if (appInstance.isUIReader()) {
                DebugUtils.removeCurrentVFileAllBreakpoint(project);
            } else if (appInstance.isCommandReader() && r.isHasResult()) {
                String[] split = r.getResult().split("\n|\r");
                variables.setListData(split);
            }
        }
    }

    /**
     * 移除断点
     * @param r r
     */
    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doRB(ExecuteResult r) {
        if (r.isSuccess()) {
            if (appInstance.isUIReader()) {
                DebugUtils.removeCurrentVFileBreakpointAtLine(project, r.getAddLine());
            }else if (appInstance.isCommandReader() && r.isHasResult()) {
                String[] split = r.getResult().split("\n|\r");
                variables.setListData(split);
            }
        }
    }

    /**
     * 该方法同样是为了拓展功能适配. [Command-Reader + UI-output]时, 才进行输出
     * @param r
     */
    @Adapt(adaptType = {ReadType.COMMAND_IN})
    private void doB_new(ExecuteResult r) {
        if (r.isSuccess() && appInstance.isCommandReader()) {
            variables.setListData(new String[]{r.getResult()});
        }
    }

    /**
     * 只有在B设置失败的情况下才触发逻辑.
     * <p>
     * B 设置失败, 需要手动清除UI界面的Breakpoint
     * @param r r
     */
    @Deprecated // 不搞这么复杂了, 如果用户想在无法设置断点处设置(比如花括号处), 随他去吧
    private void doB(ExecuteResult r) {
        if (! r.isSuccess()) {
            DebugUtils.removeCurrentVFileBreakpointAtLine(project, r.getAddLine());
        }
    }

    /**
     * 当作小trick. 从最开始的设计角度来说, P指令是服务于{Command_line_input, Console_output}, 并不适合与UI显示.
     * 但为了方便用户, 拓展不同的组合, 允许P指令在UI界面显示.
     * @param r
     */
    @Adapt(adaptType = {ReadType.COMMAND_IN, ReadType.UI_IN})
    private void doP(ExecuteResult r) {
        if (r.isSuccess()) {
            String result = r.getResult();
            String[] split = result.split("\n|\r");
            variables.setListData(split);
        } else {
            // 打印局部变量失败, 说明出问题了. 问题可能并不严重, 只是受限于debug功能
            variables.setNonData();
            String msg = r.getMsg();
            if (StringUtils.isNotBlank(msg)) {
                variables.setEmptyText(msg);
            }
        }
    }

    /**
     * 适配器, 该注解表明当前方法对不同的Reader做出适配
     */
    @interface Adapt {
        /**
         * 适配的输入类型
         * @return
         */
        ReadType[] adaptType();
    }
}