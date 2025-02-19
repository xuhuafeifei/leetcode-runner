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
        dt.addWords("һ�����");
        dt.addWords("���쿪��");
        dt.addWords("һ����ǰ");
        dt.addWords("���");
        dt.addWords("�㲻��");
        dt.addWords("һ����");
        dt.addWords("���ѽ");
        return dt;
    }
    @Test
    public void test1() {
        DictTree dt = DictTree.getInstance();
        dt.addWords("һ�����");
        dt.addWords("���쿪��");
        dt.addWords("һ����ǰ");

        System.out.println();
    }

    @Test
    public void test2() {
        DictTree dt = build();
        Hit hit = dt.match('��');
        Hit hit1 = dt.match('��', hit);
        Hit hit2 = dt.match('ѽ', hit1);
        Hit hit3 = dt.match('��', hit2);

        assert hit.isHit();
        assert '��' == hit.getC();
        assert ! hit.isEnd();

        assert hit1.isHit();
        assert '��' == hit1.getC();
        assert hit1.isEnd();

        assert hit2.isHit();
        assert 'ѽ' == hit2.getC();
        assert hit2.isEnd();

        // δ���е��ַ���Token��β
        assert ! hit3.isHit();
        assert '��' == hit3.getC();
        assert hit3.isEnd();
    }
}
