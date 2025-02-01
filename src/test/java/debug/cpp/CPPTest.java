package debug.cpp;

import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xhf.leetcode.plugin.io.file.utils.FileUtils.BackslashEscape.escapeBackslash;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CPPTest {
    private static final String methodPattern = "\\w+\\s+(\\w+)\\s*\\(([^)]*)\\)";
    private static final Pattern pattern  = Pattern.compile(methodPattern);

    @Test
    public void test() {
        Matcher matcher = pattern.matcher("bool containsNearbyDuplicate(vector<int>& nums, int k)");
        assert matcher.find();
    }

    @Test
    public void test2() {
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + 8080)
                .addJsonBody("operation", "n")
                .addJsonBody("gdbCommand", "p")
                .buildByJsonBody()
        );
        String s = httpResponse.getBody();
//        ExecuteResult executeResult = GsonUtils.fromJson(s, ExecuteResult.class);
        var a = GsonUtils.fromJson(s, ExecuteResult.class);
        String moreInfo = a.getMoreInfo();
        CppGdbInfo cppGdbInfo = GsonUtils.fromJson(moreInfo, CppGdbInfo.class);
        System.out.println(a);
        System.out.println(cppGdbInfo);
    }

    @Test
    public void test3() throws Exception {
        String cmd = "E:\\mingw-2\\mingw64\\bin\\g++.exe -g E:\\java_code\\lc-test\\cache\\debug\\cpp\\ServerMain.cpp -lws2_32 -lwsock32 -o ServerMain.exe";

        var exec = Runtime.getRuntime().exec(cmd);
        DebugUtils.printProcess(exec, false, null);
    }

    @Test
    public void test4() throws Exception {
        // 测试用例
        String input1 = "e:\\a\\b"; // 已转义，不需要处理
        String input2 = "e:\\a\\b\\"; // 最后一个反斜杠未转义
        String input3 = "e:\\a\\b\\c\\d\\"; // 最后一个反斜杠未转义
        String input4 = "e:\\a\\b\\n"; // \n 是转义字符，不需要处理
        String input5 = "e:\\a\\b\\x"; // \x 不是转义字符，需要转义

        System.out.println("输入: " + input1 + " -> 输出: " + escapeBackslash(input1));
        System.out.println("输入: " + input2 + " -> 输出: " + escapeBackslash(input2));
        System.out.println("输入: " + input3 + " -> 输出: " + escapeBackslash(input3));
        System.out.println("输入: " + input4 + " -> 输出: " + escapeBackslash(input4));
        System.out.println("输入: " + input5 + " -> 输出: " + escapeBackslash(input5));
    }
}