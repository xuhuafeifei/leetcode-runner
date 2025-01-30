package debug.cpp;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class CPPTest {
    private static final String methodPattern = "\\w+\\s+(\\w+)\\s*\\(([^)]*)\\)";
    private static final Pattern pattern  = Pattern.compile(methodPattern);

    @Test
    public void test() {
        Matcher matcher = pattern.matcher("bool containsNearbyDuplicate(vector<int>& nums, int k)");
        assert matcher.find();
    }
}
