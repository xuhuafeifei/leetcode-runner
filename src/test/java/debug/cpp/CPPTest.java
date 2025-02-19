package debug.cpp;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.CppCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.CppGdbInfo;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbElement;
import com.xhf.leetcode.plugin.debug.execute.cpp.gdb.GdbParser;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
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

    GdbParser gdbParser = GdbParser.getInstance();

    @Test
    public void test() {
        Matcher matcher = pattern.matcher("bool containsNearbyDuplicate(vector<int>& nums, int k)");
        assert matcher.find();
    }

    @Test
    public void test2() {
        HttpClient instance = HttpClient.getInstance();
        HttpResponse httpResponse = instance.executePost(new HttpRequest.RequestBuilder("http://localhost:" + 52613)
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

        var exec = DebugUtils.buildProcess(cmd);
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

    @Test
    public void test5() {
        boolean localhost = DebugUtils.isPortAvailable("localhost", 52613);
        System.out.println(localhost);
    }

    @Test
    public void test6() {
        String s = "{\"console_output\":\"\",\"log_output\":\"\",\"result_record\":\"done,stack=[frame={level=\\\"0\\\",addr=\\\"0x00007ff6a3a119b3\\\",func=\\\"main\\\",file=\\\"E:\\\\\\\\java_code\\\\\\\\lc-test\\\\\\\\cache\\\\\\\\debug\\\\\\\\cpp\\\\\\\\solution.cpp\\\",fullname=\\\"E:\\\\\\\\java_code\\\\\\\\lc-test\\\\\\\\cache\\\\\\\\debug\\\\\\\\cpp\\\\\\\\solution.cpp\\\",line=\\\"66\\\",arch=\\\"i386:x86-64\\\"}]\",\"status\":\"done\",\"stopped_reason\":\"\"}";
        CppGdbInfo cppGdbInfo = GsonUtils.fromJson(s, CppGdbInfo.class);
        System.out.println(cppGdbInfo);
    }

    @Test
    public void test7() {
        String data = "done,stack=[frame={level=\"0\",addr=\"0x00007ff6a3a119b3\",func=\"main\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"66\",arch=\"i386:x86-64\"}]";

        // 移除'done,'前缀
        data = data.replace("done,", "");

        // 将字符串调整为JSON兼容格式
        data = data.replaceAll("(\\w+)=", "\"$1\":");
        data = data.replace("\"[", "[\"");
        data = data.replace("]\"", "\"]");

        // 修复文件路径中的双反斜杠问题
        data = data.replace("\\\\", "/"); // 或者替换为四重反斜杠 \\\\\\\\ 如果需要保持原样

        // 假设每个'frame='代表一个独立的map对象，并且整个'stack'是一个list
        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(data, mapType);

        System.out.println(map);
    }

    @Test
    public void test8() {
        // 示例输入
        String input1 = "^done,stack=[frame={level=\"0\",addr=\"0x00007ff6a3a119b3\",func=\"main\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"66\",arch=\"i386:x86-64\"}]";
        String input2 = "^done,stack=[frame={level=\"0\",addr=\"0x00007ff6a3a13201\",func=\"Solution::ladderLength\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"31\",arch=\"i386:x86-64\"},frame={level=\"1\",addr=\"0x00007ff6a3a11a34\",func=\"main\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"67\",arch=\"i386:x86-64\"}]";

        // 解析
        GdbParser gdbParser = GdbParser.getInstance();
        GdbElement result1 = gdbParser.parse(input1);
        GdbElement result2 = gdbParser.parse(input2);

        // 输出
        System.out.println("解析结果 1:");
        System.out.println(result1);

        System.out.println("解析结果 2:");
        System.out.println(result2);

        String input3 = "*stopped,reason=\"breakpoint-hit\",thread-id=\"1\",frame={level=\"0\",addr=\"0x00007ff6a3a119b3\",func=\"main\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"66\",arch=\"i386:x86-64\"}";
        String s = gdbParser.preHandle(input3);
        GdbElement parse = gdbParser.parse(s);

        System.out.println(parse);
    }

    @Test
    public void test9() {
        String a = "^done,BreakpointTable={nr_rows=\"1\",nr_cols=\"6\",hdr=[{width=\"7\",alignment=\"-1\",col_name=\"number\",colhdr=\"Num\"},{width=\"14\",alignment=\"-1\",col_name=\"type\",colhdr=\"Type\"},{width=\"4\",alignment=\"-1\",col_name=\"disp\",colhdr=\"Disp\"},{width=\"3\",alignment=\"-1\",col_name=\"enabled\",colhdr=\"Enb\"},{width=\"18\",alignment=\"-1\",col_name=\"addr\",colhdr=\"Address\"},{width=\"40\",alignment=\"2\",col_name=\"what\",colhdr=\"What\"}],body=[bkpt={number=\"1\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x00007ff6a3a13201\",func=\"Solution::ladderLength(std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::vector<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> > > >&)\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"31\",thread-groups=[\"i1\"],times=\"1\",original-location=\"ladderLength\"}]}";

        // 解析
        GdbElement result1 = gdbParser.parse(a);

        System.out.println(result1);

        String b = "^done,BreakpointTable={nr_rows=\"3\",nr_cols=\"6\",hdr=[{width=\"7\",alignment=\"-1\",col_name=\"number\",colhdr=\"Num\"},{width=\"14\",alignment=\"-1\",col_name=\"type\",colhdr=\"Type\"},{width=\"4\",alignment=\"-1\",col_name=\"disp\",colhdr=\"Disp\"},{width=\"3\",alignment=\"-1\",col_name=\"enabled\",colhdr=\"Enb\"},{width=\"18\",alignment=\"-1\",col_name=\"addr\",colhdr=\"Address\"},{width=\"40\",alignment=\"2\",col_name=\"what\",colhdr=\"What\"}],body=[bkpt={number=\"1\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x0000000140003357\",func=\"Solution::ladderLength(std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::vector<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> > > >&)\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"44\",thread-groups=[\"i1\"],times=\"0\",original-location=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp:44\"},bkpt={number=\"2\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x000000014000327d\",func=\"Solution::ladderLength(std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::vector<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> > > >&)\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"34\",thread-groups=[\"i1\"],times=\"0\",original-location=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp:33\"},bkpt={number=\"3\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"0x00000001400035b3\",func=\"Solution::addEdge(std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >&)\",file=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",fullname=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp\",line=\"22\",thread-groups=[\"i1\"],times=\"0\",original-location=\"E:\\\\java_code\\\\lc-test\\\\cache\\\\debug\\\\cpp\\\\solution.cpp:22\"}]}";
        GdbElement result2 = gdbParser.parse(b);
        System.out.println(result2);
    }

    @Test
    public void test10() {
        String a = "^done,variables=[{name=\"solution\",type=\"Solution\"},{name=\"a0\",type=\"std::string\"},{name=\"a1\",type=\"std::string\"},{name=\"a2\",type=\"std::vector<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::__cxx11::basic_string<char, std::char_traits<char>, std::allocator<char> > > >\"}]";

        GdbElement result1 = gdbParser.parse(a);
        System.out.println(result1);


        // 这段gdb返回的数据是错误的, 他缺少}
        String b = "^done,variables=[{name=\"solution\",value=\"{wordId = std::unordered_map with 0 elements, edge = std::vector of length 0, capacity 0, nodeNum = 0}\"},{name=\"a0\",value=\"\\\"hit\\\"\"},{name=\"a1\",value=\"\\\"cog\\\"\"},{name=\"a2\",value=\"std::vector of length -609310409, capacity 1 = {<error reading variable: Cannot access memory at address 0x0>\"}]";
//        GdbElement r2 = gdbParser.parse(b);

//        System.out.println(r2);
    }

    @Test
    public void test11() {
        // 这个测试方法别打开, 否则很可能把一些重要的线程杀死, 这个端口仅仅用于测试杀死cpp服务
        // killProcess(63254);
    }


    @Test
    public void test12() {
        String a = "^done,{a=\"123\",b=\"456\",c=\"{addr=\"0x00007ff7d08c757c\"}\"}";
        GdbElement parse = gdbParser.parse(a);
        System.out.println(parse);
    }

    @Test
    public void test13() {
        Matcher matcher1 = CppCodeAnalyzer.pattern.matcher("vector<int> a(vector<int>& a)");
        boolean flag = matcher1.find();
        assert flag;
        assert matcher1.group(1).equals("a");
        assert matcher1.group(2).equals("vector<int>& a");

        Matcher matcher2 = CppCodeAnalyzer.pattern.matcher("int lengthOfLongestSubstring(string s)");
        boolean flag1 = matcher2.find();
        assert flag1;
        assert matcher2.group(1).equals("lengthOfLongestSubstring");
        assert matcher2.group(2).equals("string s");

        Matcher matcher3 = CppCodeAnalyzer.pattern.matcher("int minFallingPathSum(vector<vector<int>>& matrix)");
        boolean flag2 = matcher3.find();
        assert flag2;
        assert matcher3.group(1).equals("minFallingPathSum");
        assert matcher3.group(2).equals("vector<vector<int>>& matrix");

        Matcher matcher = CppCodeAnalyzer.pattern.matcher("bool isSubPath(ListNode* head, TreeNode* root)");
        boolean flag3 = matcher.find();
        assert flag3;
        assert matcher.group(1).equals("isSubPath");
        assert matcher.group(2).equals("ListNode* head, TreeNode* root");
    }

    @Test
    public void test14() {
        Matcher matcher = JavaCodeAnalyzer.pattern.matcher("public int[] demo(int[] a, int[] b)");
        boolean flag = matcher.find();
        assert flag;
        assert matcher.group(1).equals("demo");
        assert matcher.group(2).equals("int[] a, int[] b");
    }
}