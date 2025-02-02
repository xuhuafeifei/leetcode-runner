package com.xhf.leetcode.plugin.debug.execute.cpp;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.debugger.CPPDebugger;
import com.xhf.leetcode.plugin.debug.env.CppDebugEnv;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.GsonUtils;

/**
 * python client, 负责于启动debug的python服务交互
 * <p>
 * 该类只允许在Python Debug过程中使用
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CppClient {
    private final HttpClient instance;
    private final int cppPort;
    private final String url;
    public CppClient(Project project) {
        var debugger = DebugManager.getInstance(project).getDebugger(CPPDebugger.class);
        this.cppPort = ((CppDebugEnv) debugger.getEnv()).getPort();
        this.url = "http://localhost:" + cppPort;
        this.instance = HttpClient.getInstance();
    }

    public ExecuteResult postRequest(Instruction inst) {
        return postRequest(
                inst.getOperation().getName(),
                inst.getParam() == null ? "" : inst.getParam()
                );
    }

    public ExecuteResult postRequest(String operation, String gdbCommand) {
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + cppPort)
                .addJsonBody("operation", operation)
                .addJsonBody("gdbCommand", gdbCommand) // gson默认不会序列化为null字段
                .buildByJsonBody()
        );
        if (httpResponse == null) return null;
        return GsonUtils.fromJson(httpResponse.getBody(), ExecuteResult.class);
    }
}
