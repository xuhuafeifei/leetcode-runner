package com.xhf.leetcode.plugin.debug.execute.python;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.debugger.Debugger;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugger;
import com.xhf.leetcode.plugin.debug.env.DebugEnv;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

/**
 * python client, 负责于启动debug的python服务交互
 * <p>
 * 该类只允许在Python Debug过程中使用
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PyClient {
    private final HttpClient instance;
    private final int pyPort;
    private final String url;
    public PyClient(Project project) {
        PythonDebugger debugger = DebugManager.getInstance(project).getDebugger(PythonDebugger.class);
        this.pyPort = ((PythonDebugEnv) debugger.getEnv()).getPyPort();
        this.url = "http://localhost:" + pyPort + "/process";
        this.instance = HttpClient.getInstance();
    }

    public static class PyResponse {
        String status;
        String data;
        String message;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // 执行N指令
    public PyResponse executeN(Instruction inst) {
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + pyPort + "/process")
                .addJsonBody("operation", inst.getOperation().getName())
                .addJsonBody("param", inst.getParam())
                .buildByJsonBody()
        );
        return GsonUtils.fromJson(httpResponse.getBody(), PyResponse.class);
    }
}
