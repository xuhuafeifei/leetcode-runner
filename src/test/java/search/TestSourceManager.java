package search;

import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.SourceManager;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import org.junit.Test;

import java.io.FileReader;
import java.io.StringReader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestSourceManager {

    @Test
    public void test() throws Exception {
        SourceManager sm = new SourceManager();
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        sm.setSource(fr);

        Iterator itr = sm.iterator();

        try {
            while (sm.tryLoad()) {
                while (itr.hasNext()) {
                    System.out.print(itr.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void test2() throws Exception {
        SourceManager sm = new SourceManager();
        sm.setSource("s");
        try {
            /*
                当前调用必须报错, 且报错内容应该是catch块中先是的内容
                在设计时, 考虑到消费者还未消费数据源中所有的数据, sm就设置新的数据源的情况
                因此在已设置的数据源的数据被迭代消费完成前, 不允许设置新的数据源
             */
            sm.setSource("sa");
        } catch (Exception e) {
            assert e.getMessage().equals("should not set new source data when iterator haven't" +
                    " iterate all data that has been loaded!");
        }
    }

    @Test
    public void test3() throws Exception {
        SourceManager sm = new SourceManager();
        sm.setSource("s");
        try {
            Iterator itr = sm.iterator();
            assert itr.hasNext(); // 第一次为true, 因为s没有被消费
            assert 's' == itr.next();

            assert ! itr.hasNext();
            /*
                当前调用不能报错, 因为数据源的数据已经被全部消费
             */
            sm.setSource("sa");
        } catch (Exception e) {
            assert false;
        }
    }

    @Test
    public void test4() throws Exception {
        SourceManager sm = new SourceManager();
        // 测试增强迭代器
        Iterator itr = sm.iterator();
        // 测试大小写转换
        String s = "AbCd";
        sm.setSource(s);

        assert 'a' == itr.next();
        assert 'b' == itr.next();
        assert 'c' == itr.next();
        assert 'd' == itr.next();

        // 测试增强后的hasNext, 能否在迭代完成BUFFER_SIZE个数据后, 尝试从source中加载剩余数据
        s = RandomUtils.nextString(SourceManager.BUFFER_SIZE + 1);
        sm.setSource(new StringReader(s));

        // 消费buffer中的1024个数据
        for (int i = 0; i < SourceManager.BUFFER_SIZE; i++) {
            itr.next();
        }
        // 此时缓冲区已经为空, 但source中还有数据, 因此可以继续调用next(因为是SourceManager增强后的itr)
        assert itr.hasNext();
        // 消费剩下唯一一个数据
        itr.next();
        // 此时source中没有剩余数据, 因此无法继续迭代
        assert ! itr.hasNext();
    }

    /*--------------测试快照迭代器---------------------*/
    @Test
    public void test6() throws Exception {
        SourceManager sm = new SourceManager();
        // 测试快照迭代器
        Iterator itr = sm.captureIterator();

        sm.setSource("abab");
        // 加载数据
        sm.tryLoad();

        // 快照迭代器在数据加载之前创建, 此时的快照迭代器保留了未加载数据前的迭代器状态, 因此无法迭代数据
        assert ! itr.hasNext();
    }

    @Test
    public void test7() throws Exception {
        SourceManager sm = new SourceManager();
        sm.setSource("abcd");
        // 加载数据
        sm.tryLoad();

        // SourceBuffer迭代器消费2个字符
        Iterator it = sm.iterator();
        assert 'a' == it.next();
        assert 'b' == it.next();

        // 测试快照迭代器
        Iterator captureItr = sm.captureIterator();
        // 快照迭代器继续消费2个字符
        assert 'c' == captureItr.next();
        assert 'd' == captureItr.next();
        assert ! captureItr.hasNext();

        assert 'c' == it.next();

        // 在当前it的基础上拍摄快照
        captureItr = sm.captureIterator();

        assert 'd' == captureItr.next();
        assert ! captureItr.hasNext();

        assert it.hasNext();
        assert 'd' == it.next();
    }

    @Test
    public void test8() throws Exception {
        // 测试快照迭代器的hasNext方法
        String s = RandomUtils.nextString(SourceManager.BUFFER_SIZE);
        s += "`ij&";

        SourceManager sm = new SourceManager();
        sm.setSource(s);
        sm.tryLoad();

        Iterator itr = sm.iterator();
        Iterator captureItr = sm.captureIterator();

        // 迭代器迭代BUFFER_SIZE次
        for (int i = 0; i < SourceManager.BUFFER_SIZE; i++) {
            itr.next();
        }

        // 此时设置数据源, 应该是违法操作, 因为source数据没有被处理完毕
        try {
            sm.setSource("abab");
        } catch (RuntimeException e) {
            assert e.getMessage().equals("should not set new source data when iterator haven't" +
                    " iterate all data that has been loaded!");
        }

        // 此时开启快照迭代器
        captureItr = sm.captureIterator();

        // 此时captureItr加载source数据到preBuffer内
        assert captureItr.hasNext();

        assert '`' == captureItr.next();
        assert 'i' == captureItr.next();
        assert 'j' == captureItr.next();

        // source数据已经被读取到preBuffer中, 此刻source应该不存在任何数据,
        // 因此itr会从preBuffer中加载数据
        assert sm.tryLoad();
        assert itr.hasNext();
        assert '`' == itr.next();
        assert 'i' == itr.next();

        assert '&' == captureItr.next();

        captureItr = sm.captureIterator();
        assert 'j' == captureItr.next();
        assert '&' == captureItr.next();

        assert 'j' == itr.next();

        captureItr = sm.captureIterator();

        assert '&' == captureItr.next();
        assert ! captureItr.hasNext();

        assert itr.hasNext();

        assert '&' == itr.next();

        assert ! itr.hasNext();

        sm.setSource("1234");

        assert '1' == itr.next();
        assert '2' == itr.next();
        assert '3' == itr.next();

        captureItr = sm.captureIterator();

        assert '4' == captureItr.next();
        assert ! captureItr.hasNext();;

        assert itr.hasNext();

        assert '4' == itr.next();

        assert ! itr.hasNext();
    }
}
