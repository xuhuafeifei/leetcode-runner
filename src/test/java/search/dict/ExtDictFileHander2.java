package search.dict;

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
public class ExtDictFileHander2 {
    public static void main(String[] args) throws Exception{
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\search\\dict\\ext.dic");
        BufferedReader br = new BufferedReader(fr);

        List<String> res = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split("\t| ");
            if (split.length == 1) continue;
            String s = split[0];
            if (s.length() > 3) continue;
            res.add(s);
        }
        
        BufferedWriter bw = new BufferedWriter(new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\ext.dic"));
        for (String re : res) {
            bw.write(re + "\n");
        }
        bw.close();
    }
}
