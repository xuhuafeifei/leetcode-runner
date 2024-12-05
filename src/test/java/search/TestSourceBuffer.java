package search;


import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.SourceBuffer;
import com.xhf.leetcode.plugin.search.SourceManager;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import org.junit.Test;

import java.io.FileReader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestSourceBuffer {
    /**
     * 测试迭代器
     */
    @Test
    public void test() {
        String s = "jflkdajflajdl";
        SourceBuffer sourceBuffer = new SourceBuffer();
        sourceBuffer.loadFromString(s);

        Iterator itr = sourceBuffer.iterator();

        assert 'j' == itr.next(); // j
        assert 'f' == itr.next(); // f
        assert 'l' == itr.next(); // l

        assert 'l' == itr.current();
        assert 'l' == itr.current();
    }

    /**
     * 测试迭代器
     */
    @Test
    public void test2() {
        String s = "jflkdajflajdl";
        char[] charArray = s.toCharArray();
        SourceBuffer sourceBuffer = new SourceBuffer();
        Iterator itr = sourceBuffer.iterator();

        char[][] ans = new char[charArray.length][3];
        for (int i = 0; i < ans.length; i++) {
            ans[i][0] = charArray[i];
            ans[i][1] = charArray[i];
            if (i + 1 < charArray.length) {
                ans[i][2] = charArray[i + 1];
            }else {
                ans[i][2] = CharacterHelper.NULL;
            }
        }

        int i = 0;
        while (itr.hasNext()) {
            assert ans[i][0] == itr.next();
            assert ans[i][1] == itr.current();
//            if (i + 1 < charArray.length) {
                assert ans[i][2] == itr.peekNext();
//            } else {
//                assert itr.peekNext() == CharacterHelper.NULL;
//            }
            ++i;
        }
    }

    /**
     * 测试数据加载
     *
     */
    @Test
    public void test3() throws Exception {
        int size = 5;
        SourceBuffer sourceBuffer = new SourceBuffer(size);

        int cnt;
        cnt = sourceBuffer.loadFromString("12345678");
        assert "12345".equals(sourceBuffer.getBufferStr(0, cnt));
    }

    @Test
    public void test4() {
        int size = 5;
        SourceBuffer sourceBuffer = new SourceBuffer(size);

        int cnt;
        // 1. load data
        cnt = sourceBuffer.loadFromString("12345689");
        try {
            /*
             * 执行本行代码的时候必须要报错, 且报错内容应该是assert中的内容
             * 在设计的时候, 考虑了在sourceBuffer数据还没有完成迭代, 但中途插入新的数据的情况
             *
             * 例如当前测试案例中, 在1~2操作之间, 并没有消费者消费所有的数据, 因此此时的加载数据必须报错
             */
            // 2. load data without consume all
            cnt = sourceBuffer.loadFromString("12345689");
        } catch (RuntimeException e) {
            assert e.getMessage().equals("The reset method should not be executed when " +
                    "the iterator has not completed iterating over all the data.");
            return;
        }
        assert false;
    }

    @Test
    public void test5() {
        int size = 1024;
        SourceBuffer sourceBuffer = new SourceBuffer(size);

        int cnt;
        // 1. load data
        String s = "12345689";
        cnt = sourceBuffer.loadFromString(s);
        try {
            // *. 消费数据
            Iterator itr = sourceBuffer.iterator();
            for (char c : s.toCharArray()) {
                if (! itr.hasNext()) break;
                assert c == itr.next();
            }
            while (itr.hasNext()) {
                itr.next();
            }
            /*
             * 执行本行代码的时候不能报错, 区别于test4(), test5()在第二次加载数据前已经消费了所有数据
             *
             * 因此在插入数据的时候不能抛出catch块中出现的异常
             */
            // 2. load data without consume all
            cnt = sourceBuffer.loadFromString("12345689");
        } catch (RuntimeException e) {
            assert !e.getMessage().equals("The reset method should not be executed when " +
                    "the iterator has not completed iterating over all the data.");
            throw new RuntimeException("发生了其他诡异的报错, md fuck: " + e.getMessage());
        }
    }
}
