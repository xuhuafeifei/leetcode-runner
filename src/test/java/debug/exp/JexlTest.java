package debug.exp;

import com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JexlTest {
    @Test
    public void test1() {
        Object execute = JavaEvaluatorImpl.ComputeEngin.execute("1+1");
        execute = JavaEvaluatorImpl.ComputeEngin.execute("\"1234\"+1");
        System.out.println(execute);
    }
}
