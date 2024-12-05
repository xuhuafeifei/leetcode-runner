package search;

import com.xhf.leetcode.plugin.search.Iterator;
import com.xhf.leetcode.plugin.search.SourceManager;
import org.junit.Test;

import java.io.FileReader;

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
}
