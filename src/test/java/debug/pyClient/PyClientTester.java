package debug.pyClient;

import com.xhf.leetcode.plugin.debug.command.operation.Operation;
import com.xhf.leetcode.plugin.debug.execute.python.PyClient;
import com.xhf.leetcode.plugin.debug.instruction.Instruction;
import com.xhf.leetcode.plugin.debug.reader.ReadType;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import io.sentry.connection.ConnectionException;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PyClientTester {
    @Test
    public void test1() {
        Instruction inst = Instruction.success(ReadType.STD_IN, Operation.N, "1");
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = null;
        try {
            httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + 5005 + "/process")
                    .addJsonBody("operation", inst.getOperation().getName())
                    .addJsonBody("param", inst.getParam())
                    .buildByJsonBody()
            );
        } catch (ConnectionException e) {
            System.out.println();
        }
        System.out.println(GsonUtils.fromJson(httpResponse.getBody(), PyClient.PyResponse.class));
    }

    @Test
    public void test() {
        HttpClient instance = HttpClient.getInstance();
        Instruction inst = Instruction.success(ReadType.UI_IN, Operation.R, "1");
        int pyPort = 5005;
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + pyPort + "/process")
                .addJsonBody("operation", inst.getOperation().getName())
                .addJsonBody("param", inst.getParam())
                .buildByJsonBody()
        );
        if (httpResponse == null) return ;
        String body = httpResponse.getBody();
        System.out.println(body);
        PyClient.PyResponse x = GsonUtils.fromJson(body, PyClient.PyResponse.class);
        System.out.println(x);
    }

    @Test
    public void testr2() {
        for (int i = 0; i < 10; ++i) System.out.println(i);

    }

    @Test
    public void test3() {
        String s = "{\"contentJson\"\\:\"\\\\\"[]\\\\\"\",\"expireTimestamp\"\\:-1}";
        System.out.println(GsonUtils.toJsonStr(s));
    }
}
