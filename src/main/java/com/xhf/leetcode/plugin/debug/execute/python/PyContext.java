package com.xhf.leetcode.plugin.debug.execute.python;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteContext;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PyContext implements ExecuteContext {
    private Project project;
    private PythonDebugEnv env;
    private PyClient pyClient;

    public void setEnv(PythonDebugEnv env) {
        this.env = env;
    }

    public PythonDebugEnv getEnv() {
        return this.env;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public Project getProject() {
        return this.project;
    }

    public void setPyClient(PyClient pyClient) {
        this.pyClient = pyClient;
    }

    public PyClient getPyClient() {
        return this.pyClient;
    }
}
