package search;

import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.search.lucence.LCAnalyzer;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Test;

import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
            TermAttribute attribute = tokenStream.getAttribute(TermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString().substring(5));
            assert ans[i].equals(attribute.toString().substring(5));
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
            TermAttribute attribute = tokenStream.getAttribute(TermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString().substring(5));
            assert ans[i].equals(attribute.toString().substring(5));
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
            TermAttribute attribute = tokenStream.getAttribute(TermAttribute.class);
            System.out.println("ans = " + ans[i] + " attr = " + attribute.toString().substring(5));
            assert ans[i].equals(attribute.toString().substring(5));
        }
        tokenStream.end();
    }

    @Test
    public void test4() throws Exception {
        LCAnalyzer lcAnalyzer = new LCAnalyzer(10);
        FileReader reader = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        TokenStream tokenStream = lcAnalyzer.tokenStream("titleSlug", reader);

        tokenStream.reset();

        List<String> ans = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            TermAttribute attr = tokenStream.getAttribute(TermAttribute.class);
            String substring = attr.toString().substring(5);
            ans.add(substring);
            System.out.println(substring);
        }
//        System.out.println(GsonUtils.toJsonStr(ans));
        tokenStream.end();
    }

    /**
     * 该测试方法必须成功!!!!! 否则预加载机制就是错误的!!!
     * <p>
     * 预加载本质上是配合CaptureIterator, 让Processer对于底层分段数据无感知引入的一种机制. 该机制
     * 会自动处理边界数据迭代时遇到的问题. 自动加载分段数据, 且不影响底层SourceBuffer所维护的数据
     * 有关预加载机制和CaptureIterator, 可以参考如下类{@link com.xhf.leetcode.plugin.search.SourceManager}, {@link com.xhf.leetcode.plugin.search.PreBuffer}
     * {@link com.xhf.leetcode.plugin.search.SourceManager.CaptureIterator}
     * <p>
     * 为了验证预加载机制的正确性, 我们可以通过设置不同的缓冲区大小, 验证最终分词结果是否一致来判断算法的正确性.
     * 对于Processor来说, 数据在处理的时候是连续的, 因为SourceManger将分段数据在底层屏蔽, Processor是无感知的
     * 因此, 不论BufferSize如何设置, 最终的分词结果一定是一直的. 因为Processor始终认为是在处理连续数据
     * <p>
     * 因此本测试方法, 测试了预加载机制的正确性, 设定了缓冲区分别为1024 * 30, 1024, 10时分词的结果
     * 对于缓冲区为1024 * 30, 它不会触发预加载机制, 因为该缓冲区大小能够容纳所有内容. 只有缓冲区大小为1024, 10的时候
     * 才会触发. 因此, 只要后两者得到的分词结果和1024 * 30的分词结果一致, 则与加载机制正确
     *
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        LCAnalyzer lcAnalyzer = new LCAnalyzer(1024);
        FileReader reader = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        TokenStream tokenStream = lcAnalyzer.tokenStream("titleSlug", reader);

        tokenStream.reset();

        List<String> size_1024 = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            TermAttribute attr = tokenStream.getAttribute(TermAttribute.class);
            size_1024.add(attr.toString().substring(5));
        }
        tokenStream.end();

        LCAnalyzer lcAnalyzer2 = new LCAnalyzer(10);
        FileReader reader2 = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        TokenStream tokenStream2 = lcAnalyzer2.tokenStream("titleSlug", reader2);

        List<String> size_10 = new ArrayList<>();
        while (tokenStream2.incrementToken()) {
            TermAttribute attr = tokenStream2.getAttribute(TermAttribute.class);
            size_10.add(attr.toString().substring(5));
        }
        tokenStream2.end();

        LCAnalyzer lcAnalyzer3 = new LCAnalyzer(1024 * 30);
        FileReader reader3 = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\eventbus\\titleSlug.txt");
        TokenStream tokenStream3 = lcAnalyzer3.tokenStream("titleSlug", reader3);

        List<String> size_1024_30 = new ArrayList<>();
        while (tokenStream3.incrementToken()) {
            TermAttribute attr = tokenStream3.getAttribute(TermAttribute.class);
            size_1024_30.add(attr.toString().substring(5));
        }
        tokenStream2.end();

        for (int i = 0; i < size_10.size(); i++) {
//            System.out.println(size_10.get(i) + " " + size_1024.get(i));
            assert size_10.get(i).equals(size_1024.get(i)) && size_10.get(i).equals(size_1024_30.get(i));
        }
    }
}
