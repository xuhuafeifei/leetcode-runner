import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.junit.Test;

import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Demo1 {
    @Test
    public void test() {
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\main\\resources\\help\\CookieLoginHelp.md");
         
        System.out.println(s);
    }

    static LeetcodeClient instance = LeetcodeClient.getInstanceForTest();
    static HttpClient httpClient = HttpClient.getInstance();

    @Test
    public void test2() {
        List<Solution> solutions = instance.querySolutionList("smallest-range-covering-elements-from-k-lists");
        for (Solution solution : solutions) {
            System.out.println(solution.toString());
        }
    }

    @Test
    public void test3() {
        String solutionContent = instance.getSolutionContent("zui-xiao-qu-jian-by-leetcode-solution");
        System.out.println(solutionContent);
    }

    @Test
    public void test4() {
        httpClient.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImRqYW5nby5jb250cmliLmF1dGguYmFja2VuZHMuTW9kZWxCYWNrZW5kIiwiX2F1dGhfdXNlcl9oYXNoIjoiNzAwYmJmM2ZhOGQzMTNlNWJhY2EwN2NiODdiM2MyYWRjNTc4YTA2NzBkMDViODFmZDk1ZWFjNGE1N2JiZTllZiIsImlkIjozNzU2MDkwLCJlbWFpbCI6IiIsInVzZXJuYW1lIjoiYnUtY2h1YW4tbmVpLWt1LWQiLCJ1c2VyX3NsdWciOiJidS1jaHVhbi1uZWkta3UtZCIsImF2YXRhciI6Imh0dHBzOi8vYXNzZXRzLmxlZXRjb2RlLmNuL2FsaXl1bi1sYy11cGxvYWQvdXNlcnMvYnUtY2h1YW4tbmVpLWt1LWQvYXZhdGFyXzE3MTM1ODE1ODYucG5nIiwicGhvbmVfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6IjAzY2E3ZTQ4MWVkOTNlOWUwY2MyNDgxMDgyNTIzODFjIiwiaXAiOiIxLjcxLjE4Ny4xMTAiLCJfdGltZXN0YW1wIjoxNzMyMjQwMDE5LjQwMDgzODEsImV4cGlyZWRfdGltZV8iOjE3MzQ4MDc2MDB9.acwry54rbuFj_v1T-HJtx--HbZUM3t-ZsDdNwvK0zmk"));
        List<Submission> submissionList = instance.getSubmissionList("smallest-range-covering-elements-from-k-lists");

        for (Submission submission : submissionList) {
            System.out.println(submission.toString());
        }

//        for (Submission submission : submissionList) {
////            System.out.println("submission = " + GsonUtils.toJsonStr(submission));
//            String submissionCode = instance.getSubmissionCode(submission.getId());
//            System.out.println("submissionCode = " + submissionCode);
//        }
    }
}
