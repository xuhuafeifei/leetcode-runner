package search;

import com.xhf.leetcode.plugin.search.dict.DictTree;
import com.xhf.leetcode.plugin.search.dict.Hit;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestDict {
    private DictTree build() {
        DictTree dt = DictTree.getInstance();
        dt.addWords("一如既往");
        dt.addWords("天天开心");
        dt.addWords("一往无前");
        dt.addWords("你好");
        dt.addWords("你不好");
        dt.addWords("一天天");
        dt.addWords("你好呀");
        return dt;
    }
    @Test
    public void test1() {
        DictTree dt = DictTree.getInstance();
        dt.addWords("一如既往");
        dt.addWords("天天开心");
        dt.addWords("一往无前");

        System.out.println();
    }

    @Test
    public void test2() {
        DictTree dt = build();
        Hit hit = dt.match('你');
        Hit hit1 = dt.match('好', hit);
        Hit hit2 = dt.match('呀', hit1);
        Hit hit3 = dt.match('吗', hit2);

        assert hit.isHit();
        assert '你' == hit.getC();
        assert ! hit.isEnd();

        assert hit1.isHit();
        assert '好' == hit1.getC();
        assert hit1.isEnd();

        assert hit2.isHit();
        assert '呀' == hit2.getC();
        assert hit2.isEnd();

        // 未命中的字符是Token结尾
        assert ! hit3.isHit();
        assert '吗' == hit3.getC();
        assert hit3.isEnd();
    }
}
