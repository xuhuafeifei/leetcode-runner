package search;

import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.StringReader;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LCAnalyzerTest {

    /**
     * 测试数字分词器
     */
    @Test
    public void test1() throws Exception {
        LCAnalyzer lcAnalyzer = new LCAnalyzer();
        StringReader reader = new StringReader("&&&  123.168.9.10 ---  ");
        TokenStream tokenStream = lcAnalyzer.tokenStream("ip", reader);

        tokenStream.reset();
        String[] ans = new String[] {"123", "168", "9", "10"};
        for (int i = 0; i < 4; i++) {
            tokenStream.incrementToken();
            CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString());
            assert ans[i].equals(attribute.toString());
        }
        tokenStream.end();
    }

    /**
     * 测试字母分词器
     */
    @Test
    public void test2() throws Exception {
        LCAnalyzer lcAnalyzer = new LCAnalyzer();
        StringReader reader = new StringReader("&&&  this is a letter context ---  ");
        TokenStream tokenStream = lcAnalyzer.tokenStream("ip", reader);

        tokenStream.reset();
        String[] ans = new String[] {"this", "is", "a", "letter", "context"};
        for (int i = 0; i < ans.length; i++) {
            tokenStream.incrementToken();
            CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString());
            assert ans[i].equals(attribute.toString());
        }
        tokenStream.end();
    }

    /**
     * 测试字母 + 数字分词器
     */
    @Test
    public void test3() throws Exception {
        LCAnalyzer lcAnalyzer = new LCAnalyzer();
        StringReader reader = new StringReader("&&&  129.this.137 is 8 & a letter context ---  ");
        TokenStream tokenStream = lcAnalyzer.tokenStream("ip", reader);

        tokenStream.reset();
        String[] ans = new String[] {"129", "this", "137", "is", "8", "a", "letter", "context"};
        for (int i = 0; i < ans.length; i++) {
            tokenStream.incrementToken();
            CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString());
            assert ans[i].equals(attribute.toString());
        }
        tokenStream.end();
    }
}
