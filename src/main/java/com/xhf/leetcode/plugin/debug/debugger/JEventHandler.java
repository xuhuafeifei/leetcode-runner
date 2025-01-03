package com.xhf.leetcode.plugin.debug.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.execute.java.JavaBInst;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.InstSource;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.ViewUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * java debug event handler
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JEventHandler implements Runnable {
    private final Context context;
    private final Project project;
    private final JavaDebugEnv env;

    public JEventHandler(Context ctx) {
        this.context = ctx;
        this.project = context.getProject();
        this.env = ctx.getEnv();
        new Thread(this, "JEventHandler").start();
    }

    @Override
    public void run() {
        EventQueue eventQueue = context.getVm().eventQueue();
        while (true) {
            try {
                // System.err.println("准备阻塞...");
                EventSet set = eventQueue.remove();
                // System.err.println("获取数据, 不再阻塞....");
                Iterator<Event> it = set.iterator();
                // copy JDB源码
                boolean resumeStoppedApp = false;

                while (it.hasNext()) {
                    // 只要有一个事件不resume, 就必须resume
                    resumeStoppedApp |= !handleEvent(it.next());
                }

                if (resumeStoppedApp) {
                    set.resume();
                }
            } catch (VMDisconnectedException ignored) {
                LogUtils.simpleDebug("debug stop...");
                return;
            } catch (InterruptedException | AbsentInformationException e) {
                LogUtils.error(e);
            }
        }
    }

    /**
     * 处理event, 同时返回是否resume
     *
     * @param event
     * @return false: 需要resume; true: 不需要resume, 需要阻塞
     * @throws AbsentInformationException
     */
    private boolean handleEvent(Event event) throws AbsentInformationException {
        // debug
        // DebugUtils.simpleDebug(event.toString(), project);
        // 设置context基本信息
        if (event instanceof LocatableEvent) {
            setContextBasicInfo((LocatableEvent) event);
        }

        if (event instanceof ClassPrepareEvent) {
            return handleClassPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof BreakpointEvent) {
            return handleBreakpointEvent((BreakpointEvent) event); // 需要停止
        } else if (event instanceof StepEvent) {
            return handleStepEvent((StepEvent) event); // 需要停止
        } else if (event instanceof VMStartEvent) {
            return false;
        } else if (event instanceof VMDisconnectEvent) {
            return handleVMDisconnectEvent((VMDisconnectEvent) event);
        } else {
            return true;
        }
    }



    private boolean handleVMDisconnectEvent(VMDisconnectEvent event) {
        DebugManager.getInstance(project).stopDebugger();
        return true;
    }

    private boolean handleStepEvent(StepEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());

        Thread.yield();  // fetch output

        // 后台准备完毕, 前台可以执行指令
        context.done();
        return true;
    }

    private void commandBreakpointInit(ClassPrepareEvent classPrepareEvent) {
        // 获取solutionClass
        ClassType solutionClass = (ClassType) classPrepareEvent.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        Location location = mainMethod.location();

        context.setLocation(location);
        context.setSolutionLocation(location);

        // 设置断点
        new JavaBInst().execute(Instruction.success(ReadType.UI_IN, Operation.B, String.valueOf(location.lineNumber())), context);

        DebugUtils.simpleDebug("break point set at Solution." + mainMethod + " line " + location.lineNumber(), project);
    }

    private void uiBreakpointInit(ClassPrepareEvent classPrepareEvent) {
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
                new JavaBInst().execute(instruction, context);
            }
        }
    }

    private boolean handleBreakpointEvent(BreakpointEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());

        context.setBreakpointEvent(event);
        context.setThread(event.thread());
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_OVER);

        Location location = event.location();
        String className = location.declaringType().name(); // 类名
        String methodName = location.method().name(); // 方法名
        int lineNumber = location.lineNumber(); // 行号

        String res = className + "." + methodName + ":" + lineNumber;

        DebugUtils.simpleDebug("Hit breakpoint at: " + res, project);

        context.setBreakpointEvent(event);

        // 设置单步请求
        setContextBasicInfo(event); // 这是初始化方法, 不能删除...
        context.setStepRequest(StepRequest.STEP_LINE, StepRequest.STEP_INTO);

        if (AppSettings.getInstance().isUIOutput()) {
            // 输入UI打印指令. UI总是会在遇到断点时打印局部变量. 因此输入P指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.P, ""));
            // 高亮指令
            InstSource.uiInstInput(Instruction.success(ReadType.UI_IN, Operation.W, ""));
        }

        // 后台准备完毕, 前台可以执行指令
        context.done();
        return true;
    }

    private boolean handleClassPrepareEvent(ClassPrepareEvent event) {
        String className = event.referenceType().name();
        if (className.equals("Solution")) {
            Location location = context.setSolutionLocation(event);
            context.setLocation(location);
            context.setThread(event.thread());

            // ui读取模式下, 初始化断点
            if (AppSettings.getInstance().isUIReader()) {
                uiBreakpointInit(event);
            }else {
                commandBreakpointInit(event);
            }
        }
        return false;
    }

    /**
     * 设置基础信息, 比如location和thread
     *
     * @param event
     */
    private void setContextBasicInfo(LocatableEvent event) {
        context.setLocation(event.location());
        context.setThread(event.thread());
    }
}