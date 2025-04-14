package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.*;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbElement;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbParser;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.output.OutputType;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CPPDebugger extends AbstractDebugger {

    private final CppContext context;
    private final CppDebugConfig config;
    private final ReadType readType;
    private final OutputType outputType;
    private CppDebugEnv env;
    private Process exec;

    public CPPDebugger(Project project, CppDebugConfig config) {
        super(project, new CppContext(project), config, CppInstFactory.getInstance());
        this.context = (CppContext) super.basicContext;
        this.config = config;
        this.readType = config.getReadType();
        this.outputType = config.getOutputType();
        // this.outputHelper = new OutputHelper(project);
    }

    @Override
    public void start() {
        this.env = new CppDebugEnv(project);
        boolean flag = super.envPrepare(env);
        if (! flag) {
            return;
        }
        // 需要开启新线程, 否则指令读取操作会阻塞idea渲染UI的主线程
        new Thread(this::startDebug).start();
    }

    private void startDebug() {
        env.startDebug();
        try {
            startCppService();
            initCtx();
            executeCppDebugRemotely();
        } catch (DebugError e) {
            LogUtils.error(e);
            ConsoleUtils.getInstance(project).showError(e.toString(), false, true);
        }
        DebugManager.getInstance(project).stopDebugger();

    }

    private void executeCppDebugRemotely() {
        initBreakpoint();
        doRun();
    }

    private void doRun() {
        // 初始化运行指令
        ExecuteResult r = new AbstractCppInstExecutor() {
            @Override
            protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
                return "-exec-run";
            }
        }.execute(Instruction.success(this.readType, Operation.R, ""), context);

        doAfterExecuteInstruction(r);

        while (DebugManager.getInstance(project).isDebug()) {
            ProcessResult pR = processDebugCommand();
            if (pR.isContinue) {
                continue;
            } else if (pR.isReturn) {
                return;
            }
            if (!pR.isSuccess) {
                throw new DebugError(BundleUtils.i18n("action.leetcode.unknown.error"));
            }
        }
    }

    @Override
    protected void doAfterExecuteInstruction(ExecuteResult r) {
        // 判断是否为r指令, 同时判断是否stop
        if (r.getOperation() == Operation.R) {
            GdbParser instance = GdbParser.getInstance();
            CppGdbInfo cppGdbInfo = GsonUtils.fromJson(r.getMoreInfo(), CppGdbInfo.class);
            // 异常判断
            if (cppGdbInfo == null) {
                DebugUtils.simpleDebug(BundleUtils.i18nHelper("cpp debug出现异常, 终止debug", "some error happen in cpp debug, the flow will stop soon"), project, ConsoleViewContentType.ERROR_OUTPUT, true);
                DebugManager.getInstance(project).stopDebugger();
                return;
            }
            if (!"error".equals(cppGdbInfo.getStatus())) {
                // 执行正确
                GdbElement ele = instance.parse(instance.preHandle(cppGdbInfo.getStoppedReason()));
                String reason = ele.getAsGdbObject().get("reason").getAsGdbPrimitive().getAsString();
                // 不是因为breakpoint终止, 那么就是GDB完成运行或者出现异常, 需要停止debug
                if (! "breakpoint-hit".equals(reason)) {
                    DebugManager.getInstance(project).stopDebugger();
                }
            } else {
                // 执行错误
                GdbElement ele = instance.parse(instance.preHandle(cppGdbInfo.getResultRecord()));
                String msg = ele.getAsGdbObject().get("msg").getAsGdbPrimitive().getAsString();
                if ("The program is not being run.".equals(msg)) {
                    DebugManager.getInstance(project).stopDebugger();
                }
            }
        }
    }

    private void initBreakpoint() {
        // ui读取模式下, 初始化断点
        if (AppSettings.getInstance().isUIReader()) {
            uiBreakpointInit();
        }else {
            commandBreakpointInit();
        }
    }

    private void commandBreakpointInit() {
        new CppBInst().execute(Instruction.success(ReadType.COMMAND_IN, Operation.B, env.getMethodName()), context);
    }

    private void uiBreakpointInit() {
        // 获取所有断点
        List<XBreakpoint<?>> allBreakpoint = DebugUtils.getAllBreakpoint(project);
        for (XBreakpoint<?> breakpoint : allBreakpoint) {
            XSourcePosition position = breakpoint.getSourcePosition();
            if (position == null) {
                continue;
            }
            VirtualFile file = Objects.requireNonNull(position).getFile();
            // 如果file和当前打开的vile一致, 设置断点信息
            if (file.equals(ViewUtils.getCurrentOpenVirtualFile(project))) {
                Instruction instruction = DebugUtils.buildBInst(position);
                // 设置断点
                new CppBInst().execute(instruction, context);
            }
        }
        // 遇到断点后, 高亮显示
        InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, null));
        // 遇到断点后, 打印变量
        InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, null));
    }

    private void initCtx() {
        this.context.setEnv(env);
        this.context.setCppClient(new CppClient(project));
        this.context.setReadType(this.readType);
    }

    private void startCppService() {
        String serverMainExePath = env.getServerMainExePath();
        StringBuilder sb = new StringBuilder(serverMainExePath);
        for (String arg : env.getServerArgv()) {
            sb.append(" ").append(arg);
        }

        String cmd = sb.toString();
        // 如果是mac/linux, 先修改权限, 添加执行权限
        if (! OSHandler.isWin()) {
            cmd = "chmod 744 " + serverMainExePath + " && " + cmd;
        }

        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.server.start.cmd") + ": " + cmd, project);

        try {
            this.exec = OSHandler.buildProcess(cmd);
            DebugUtils.printProcess(exec, true, project);
        } catch (Exception e) {
            throw new DebugError(BundleUtils.i18n("debug.leetcode.server.connect.failed") + "\n" + e.getCause() + "\n" + BundleUtils.i18n("debug.leetcode.instruction") + " = " + cmd);
        }

        // 五次检测连接(3s还连接不上, 挂了)
        for (int i = 0; i < 6; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            if (DebugUtils.isPortAvailable2("localhost", env.getPort())) {
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.server.connect.succuess"), project, false);
                return;
            }
        }
        throw new DebugError(BundleUtils.i18n("debug.leetcode.server.connect.failed"));
    }

    @Override
    public void stop() {
        // 已经停止了, 无需再次停止
        if (!DebugManager.getInstance(project).isDebug()) {
            return;
        }
        // 发送终止命令, 并关停debug
        CppClient cppClient = this.context.getCppClient();
        if (cppClient != null) {
            new AbstractCppInstExecutor() {
                @Override
                protected String getGdbCommand(@NotNull Instruction inst, CppContext pCtx) {
                    return "quit";
                }
            }.execute(Instruction.success(this.readType, Operation.R, ""), context);
        }

        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stopsoon"), project);
        env.stopDebug();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        // 如果没有启动, 直接返回
        if (!DebugUtils.isPortAvailable2("localhost", env.getPort())) {
            DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project);
            return;
        }

        // 3次循环, 检测cpp服务是否已经关闭
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            if (!DebugUtils.isPortAvailable2("localhost", env.getPort())) {
                DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project);
                return;
            }
        }
        exec.destroy();
        if (exec.isAlive()) {
            KillPortProcess.killProcess(env.getPort());
        }
        DebugUtils.simpleDebug(BundleUtils.i18n("debug.leetcode.debug.server.stop"), project, false);
    }

    @Override
    public DebugEnv getEnv() {
        return this.env;
    }
}
