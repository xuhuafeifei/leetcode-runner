package search;

import com.xhf.leetcode.plugin.search.dict.Hit;
import com.xhf.leetcode.plugin.search.process.CNProcessor;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import org.jetbrains.deft.Obj;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestSimpleHitStack {
    @Test
    public void test1() {
        CNProcessor.SimpleHitStack stack = new CNProcessor.SimpleHitStack();
        Hit[] hits = new Hit[15];
        String s = RandomUtils.nextString(15);
        for (int i = 0; i < 15; i++) {
            hits[i] = Hit.notHit(s.charAt(i));
            stack.add(hits[i]);
        }
        assert s.equals(stack.getToken());

        try {
            stack.add(new Hit());
        } catch (RuntimeException e) {
            assert e.getMessage().equals("stack is full !");
        }

        for (int i = 0; i < 15; i++) {
            assert hits[14 - i].getC() == stack.pop().getC();
            assert s.substring(0, 14 - i).equals(stack.getToken());
        }

        try {
            stack.pop();
        } catch (RuntimeException e) {
            assert e.getMessage().equals("stack is empty !");
        }
    }
}
