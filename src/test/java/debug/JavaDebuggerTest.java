package debug;

import com.xhf.leetcode.plugin.debug.debugger.JavaDebugConfig;
import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import com.xhf.leetcode.plugin.debug.execute.InstExecutor;
import com.xhf.leetcode.plugin.debug.execute.JavaInstFactory;
import com.xhf.leetcode.plugin.debug.output.StdOutput;
import com.xhf.leetcode.plugin.debug.params.InstParserImpl;
import com.xhf.leetcode.plugin.debug.params.Instrument;
import com.xhf.leetcode.plugin.debug.reader.InstReader;
import com.xhf.leetcode.plugin.debug.reader.StdInReader;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebuggerTest {
    @Test
    public void test() {

    }

    public static void main(String[] args) {
        t1();
//        t2();
    }

    public static void t1(){
        JavaDebugger javaDebugger = new JavaDebugger(null, new JavaDebugConfig(new StdInReader(), new StdOutput()));
        javaDebugger.start();
    }

    public static void t2(){

        String res;
        InstReader reader = new StdInReader();
        while (true) {
            res = reader.readInst();
            if (res.equals("bk") || res.equals("exit")) {
                return;
            }
            // 解析指令, 并执行
            Instrument parse = new InstParserImpl().parse(res);
            // debug parse
            if (parse == null) {
                LogUtils.simpleDebug("指令解析失败");
                continue;
            }
            LogUtils.simpleDebug(parse.toString());
            JavaInstFactory instance = JavaInstFactory.getInstance();
            InstExecutor instExecutor = instance.create(parse);
            // debug
            LogUtils.simpleDebug(instExecutor.getClass().getName());
        }
    }

}
