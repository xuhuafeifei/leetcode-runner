import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Demo1 {
    @Test
    public void test() {
        String s = FileUtils.readContentFromFile("E:\\java_code\\leetcode-runner\\src\\main\\resources\\help\\CookieLoginHelp.md");
        System.out.println(s);
    }
}
