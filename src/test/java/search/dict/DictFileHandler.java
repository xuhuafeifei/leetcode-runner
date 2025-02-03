package search.dict;

import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class DictFileHandler {
    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\main.dic");
        BufferedReader br = new BufferedReader(fr);

        List<String> res = new ArrayList<>(2500);

        String line;
        while ((line = br.readLine()) != null) {
            if (StringUtils.isBlank(line)) continue;
            String[] split = line.split(" ");
            if (split.length == 1) continue;
            // 除去一开始的数字
            int i = 1;
            for (; i < split.length; i++) {
                String s = split[i];
                boolean flag = false;
                for (int j = 0; j < s.length(); ++j) {
                    char c = s.charAt(j);
                    if (CharacterHelper.isCJKCharacter(c)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            }
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < split.length; ++j) {
                sb.append(split[j]);
                if (j != split.length - 1) sb.append(" ");
            }
            res.add(sb.toString());
        }


        FileWriter fw = new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle.dic");
        BufferedWriter bw = new BufferedWriter(fw);

        for (String re : res) {
            bw.write((re + "\n"));
        }
    }
}
