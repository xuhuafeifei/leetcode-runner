package search.dict;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.commons.lang.StringUtils;
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
public class DictFileHandler6 {
    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle5.dic");
        BufferedReader br = new BufferedReader(fr);

        Set<String> res = new HashSet<>(1200);

        String line;
        while ((line = br.readLine()) != null) {
            if (StringUtils.isNotBlank(line) && line.length() < 10 && line.length() > 1) {
                res.add(line);
            }
        }

        FileWriter fw = new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\main.dic");
        BufferedWriter bw = new BufferedWriter(fw);

        for (String re : res) {
            bw.write((re + "\n"));
        }

        bw.close();
    }
}
