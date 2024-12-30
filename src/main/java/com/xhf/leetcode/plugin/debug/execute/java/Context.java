package com.xhf.leetcode.plugin.debug.execute.java;

import com.intellij.openapi.project.Project;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.xhf.leetcode.plugin.debug.env.JavaDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;
import com.xhf.leetcode.plugin.exception.DebugError;

import java.util.Iterator;
import java.util.List;

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
     * 单步请求管理器
     */
    private final StepRequestManager stepRequestManager = new StepRequestManager();
    private Iterator<Event> itr;

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

    public Location getLocation() {
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

    public ThreadReference getThread() {
		return thread;
	}

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return this.project;
    }

    public void setSolutionLocation(Location location) {
        this.solutionLocation = location;
    }

    public Location getSolutionLocation() {
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
}
