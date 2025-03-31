import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.model.Submission;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Demo1 {
    @Test
    public void test() {
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\main\\resources\\help\\CookieLoginHelp.md");
        System.out.println(s);
        Arrays.sort(new int[]{1, 2, 3});
        HashMap<Object, Object> map = new HashMap<>();
        map.put(null, "");
        Hashtable<Object, Object> ht = new Hashtable<>();
        ht.put(null, null);

        List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(2);
        list.add(1);
        list.sort((Integer a, Integer b) -> {
            return a - b;
        });
    }

    class A {
        public A a() {
            return null;
        }
    }

    class B extends A {
        public B a() {
            return null;
        }
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

    @Test
    public void test5() throws Exception {
        List<String> collect = instance.queryTotalQuestion().stream().map(e -> e.toString()).collect(Collectors.toList());
        File file = new File("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        FileOutputStream fos = new FileOutputStream(file);
        for (String s : collect) {
            System.out.println(s);
            fos.write((s + '\n').getBytes(StandardCharsets.UTF_8));
        }
        fos.close();
    }

    public static void main(String[] args) throws Exception {
        // 创建分词器
        Analyzer analyzer = new Analyzer() {
            @Override
            public TokenStream tokenStream(String fieldName, Reader reader) {
                // 按照空格分词的Tokenizer
                return new WhitespaceTokenizer(reader);
            }
        };
        // 获取Tokenizer(它继承TokenStream, 并且只接受Reader作为输入)
        TokenStream ts = analyzer.tokenStream("test", new StringReader("this is a apple !!"));

        ts.reset();
        while (ts.incrementToken()) {
            // 获取ts分词枚举的token
            TermAttribute attr = ts.getAttribute(TermAttribute.class);
            System.out.println(attr.toString());
        }
        ts.end();
        ts.close();
    }
}
