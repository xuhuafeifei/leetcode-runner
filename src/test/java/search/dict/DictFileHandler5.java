package search.dict;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlgraphics.image.codec.util.SingleTileRenderedImage;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DictFileHandler5 {

    /**
     * 测试CNExtract
     */
    @Test
    public void test1() {
        List<String> strings = CNExtract("48032 你((好呀, 这是一个:JO测试（（的方法!!");
        String[] ans = {"你", "好呀", "这是一个", "测试", "的方法"};

        assert Objects.requireNonNull(strings).size() == 5;
        for (int i = 0; i < 5; i++) {
            assert Objects.equals(strings.get(i), ans[i]);
        }

        assert null == CNExtract("fdab");
        List<String> strings1 = CNExtract("你好***");
        assert Objects.requireNonNull(strings1).size() == 1;
        assert "你好".equals(strings1.get(0));
    }

    public static List<String> CNExtract(String line) {
        List<String> res = new ArrayList<>();
        int len = line.length();
        Deque<int[]> dq = new ArrayDeque<>();
        for (int i = 0; i < len; ++i) {
            char c = CharacterHelper.regularize(line.charAt(i));
            if (CharacterHelper.isCJKCharacter(c)) {
                int[] ints = {i, c};
                dq.addLast(ints);
            }
        }
        if (dq.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        int[] first = dq.pollFirst();
        int last = first[0];
        char c = (char) first[1];
        sb.append(c);

        while (dq.size() != 0) {
            int[] node = dq.pollFirst();
            int idx = node[0];
            c = (char) node[1];
            if (idx == last + 1) {
                sb.append(c);
            }else {
                res.add(sb.toString());
                sb.delete(0, sb.length());
                sb.append(c);
            }
            last = idx;
        }
        res.add(sb.toString());
        return res;
    }

    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle4.dic");
        BufferedReader br = new BufferedReader(fr);

        List<String> res = new ArrayList<>(2500);

        String line;
        while ((line = br.readLine()) != null) {
            if (StringUtils.isNotBlank(line)) {
                List<String> strs = CNExtract(line);
                if (strs == null) continue;
                for (String str : strs) {
                    res.add(str);
                }
            }
        }

        FileWriter fw = new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle5.dic");
        BufferedWriter bw = new BufferedWriter(fw);

        for (String re : res) {
            bw.write((re + "\n"));
        }

        bw.close();
    }
}
