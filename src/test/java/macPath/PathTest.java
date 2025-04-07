package macPath;

import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import org.junit.Test;

public class PathTest {

    @Test
    public void test() {
        boolean path = FileUtils.isPath("/Users/xhf/Desktop/");
        assert path;
        path = FileUtils.isPath("/Users/xuhuafei/fbgb/java_code/lc-test/cache/app.properties");
        assert path;
    }
}
