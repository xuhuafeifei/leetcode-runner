package com.xhf.leetcode.plugin.debug.execute.java;

import com.intellij.openapi.project.Project;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 指令执行上下文
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Context implements ExecuteContext {
    private BreakpointEvent breakpointEvent;
    private String output;
    private EventRequestManager erm;
    private EventSet eventSet;
    private ClassType currentClass;
    private VirtualMachine vm;
    private JavaDebugEnv env;
    /**
     * 表示当前代码执行的location
     */
    private Location location;
    private List<BreakpointRequest> breakpointRequests;
    private ThreadReference thread;
    private Project project;
    /**
     * 专门表示Solution类的location
     */
    private Location solutionLocation;
    /**
     * 单步请求管理器. 统一管理所有的单步请求
     */
    private final StepRequestManager stepRequestManager = new StepRequestManager();
    private Iterator<Event> itr;
    private volatile boolean waitFor = false;

    public ClassType getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(ClassType currentClass) {
        this.currentClass = currentClass;
    }

    public BreakpointEvent getBreakpointEvent() {
        return breakpointEvent;
    }

    public void setBreakpointEvent(BreakpointEvent breakpointEvent) {
        this.breakpointEvent = breakpointEvent;
    }

    public String getOutput() {
        String res = output;
        output = null;
        return res;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public EventRequestManager getErm() {
        return erm;
    }

    public void setErm(EventRequestManager erm) {
        this.erm = erm;
    }

    public void addOutput(String res) {
        this.output = res;
    }

    public EventSet getEventSet() {
		return eventSet;
	}

    public void setEventSet(EventSet eventSet) {
		this.eventSet = eventSet;
	}

    public void setStepRequest(int size, int depth) {
        stepRequestManager.setStepRequest(size, depth);
    }

    /**
     * 这个名字不好, 取错了, 但考虑该方法已经大面积使用, 就保留了
     */
    public void removeStepRequest() {
        stepRequestManager.disable();
    }

    public void disableStepRequest() {
        stepRequestManager.disable();
    }

    public void enableStepRequest() {
        stepRequestManager.enable();
    }

    public void setVm(VirtualMachine vm) {
        this.vm = vm;
    }

    public VirtualMachine getVm() {
		return vm;
	}

    public void setEnv(JavaDebugEnv env) {
        this.env = env;
    }

    public JavaDebugEnv getEnv() {
		return env;
	}

    public void setLocation(Location location) {
        this.location = location;
    }

    public synchronized Location getLocation() {
		return location;
	}

    public void setBreakpointRequestList(List<BreakpointRequest> breakpointRequests) {
        this.breakpointRequests = breakpointRequests;
    }

    public List<BreakpointRequest> getBreakpointRequests() {
		return breakpointRequests;
	}

    public void addBreakpointRequest(BreakpointRequest breakpointRequest) {
        this.breakpointRequests.add(breakpointRequest);
    }

    public void setThread(ThreadReference thread) {
        this.thread = thread;
    }

    public synchronized ThreadReference getThread() {
		return thread;
	}

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return this.project;
    }

    public Location setSolutionLocation(ClassPrepareEvent event) {
        String className = event.referenceType().name();
        if (!className.equals("Solution")) {
            throw new DebugError("Context setSolutionLocation使用错误. 该方法只允许当className为Solution时使用!");
        }
        // 获取solutionClass
        ClassType solutionClass = (ClassType) event.referenceType();
        // 获取solution类的核心方法
        String methodName = env.getMethodName();
        Method mainMethod = solutionClass.methodsByName(methodName).get(0);
        // 获取location
        Location location = mainMethod.location();
        setSolutionLocation(location);
        return location;
    }

    public void setSolutionLocation(Location location) {
        this.solutionLocation = location;
    }

    public synchronized Location getSolutionLocation() {
        return this.solutionLocation;
    }

    public void setItrEvent(Iterator<Event> itr) {
        this.itr = itr;
    }

    public Iterator<Event> getItrEvent() {
        return this.itr;
    }

    public void consumeAllEvent() {
        while (this.itr != null && this.itr.hasNext()) {
            itr.next();
        }
    }

    public void resume() {
        if (vm == null) {
            throw new DebugError("Context resume使用错误. 请在通过Context恢复Target VM运行前, 先设置vm对象 !");
        }
        vm.resume();
    }

    private class StepRequestManager {
        private StepRequest stepRequest;
        void setStepRequest(int size, int depth) {
            if (stepRequest != null) {
                this.stepRequest.disable();
            }
            if (erm == null || thread == null) {
                throw new DebugError("Context setStepRequest使用错误. 请在使用context对StepRequest进行统一管理前, 先设置erm和thread对象 !");
            }
            this.stepRequest = erm.createStepRequest(thread, size, depth);
            this.stepRequest.enable();
        }
        void disable() {
            if (stepRequest != null) {
                stepRequest.disable();
            }
        }

        public void enable() {
            if (stepRequest != null) {
                stepRequest.enable();
            }
        }
    }

    /**
     * 锁
     */
    private final Object lock = new Object();
    /**
     * 存储调用waitFor的线程
     */
    private final Set<Thread> threadSet = new HashSet<>();

    /**
     * 自选等待JEventHandler通知, 如果waitFor为False或者被打断/唤醒, 则表明JEventHandler完成处理
     * JavaDebugger可以继续执行
     */
    public void waitForJEventHandler(String name) {
        // 总是先让出cpu
        Thread.yield();
        // 无需等待
        if (! waitFor) {
            return;
        }
        synchronized (lock) {
            LogUtils.simpleDebug(name + "等待JEventHandler...");
            threadSet.add(Thread.currentThread());
            try {
                // 自旋优化
                for (int i = 0; i < 8 && waitFor; ++i) {
                    Thread.sleep(100);
                }
                LogUtils.simpleDebug("睡眠...");
                lock.wait();
            } catch (InterruptedException e) {
                LogUtils.simpleDebug("JEventHandler释放锁, " + name + "执行指令...");
            }
        }
    }

    /**
     * 调用此方法, JavaDebugger将会等待JEventHandler解锁
     * 该方法服务于JavaDebugger. 其他类不能调用此方法
     */
    public void waitFor() {
        synchronized (lock) {
            this.waitFor = true;
        }
    }

    /**
     * 调用此方法, 表示JEventHandler已经完成运行, JavaDebugger无需等待
     * 该方法服务于JEventHandler. 其他类不能调用此方法
     */
    public void done() {
        this.waitFor = false;
        for (Thread t : threadSet) {
            t.interrupt();
        }
        synchronized (lock) {
            this.lock.notifyAll();
        }
    }
}