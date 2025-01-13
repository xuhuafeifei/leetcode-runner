package com.xhf.leetcode.plugin.debug.execute.python;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.DebugManager;
import com.xhf.leetcode.plugin.debug.debugger.PythonDebugger;
import com.xhf.leetcode.plugin.debug.env.PythonDebugEnv;
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
        ExecuteResult data;
        String message;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public ExecuteResult getData() {
            return data;
        }

        public void setData(ExecuteResult r) {
            this.data = r;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "PyResponse{" +
                    "status='" + status + '\'' +
                    ", data=" + data +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public PyResponse postRequest(Instruction inst) {
        return postRequest(
                inst.getOperation().getName(),
                inst.getParam() == null ? "" : inst.getParam()
                );
    }

    public PyResponse postRequest(String operation, String param) {
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + pyPort + "/process")
                .addJsonBody("operation", operation)
                .addJsonBody("param", param) // gson默认不会序列化为null字段
                .buildByJsonBody()
        );
        if (httpResponse == null) return null;
        return GsonUtils.fromJson(httpResponse.getBody(), PyResponse.class);
    }
}
