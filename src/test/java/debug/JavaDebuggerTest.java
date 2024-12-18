package debug;

import com.xhf.leetcode.plugin.debug.debugger.JavaDebugger;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaDebuggerTest {
    @Test
    public void test() {
        JavaDebugger javaDebugger = new JavaDebugger(null);
        javaDebugger.start();
    }

    public static void main(String[] args) {
        JavaDebugger javaDebugger = new JavaDebugger(null);
        javaDebugger.start();
    }
}
