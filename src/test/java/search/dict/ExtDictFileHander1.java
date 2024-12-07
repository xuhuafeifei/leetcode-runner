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
public class ExtDictFileHander1 {
    public static void main(String[] args) throws Exception{
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\test\\java\\search\\dict\\sogouw.dict.yaml");
        BufferedReader br = new BufferedReader(fr);

        List<String> res = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ");
            if (split.length == 1) continue;
            res.add(split[0]);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\ext.dic"));
        for (String re : res) {
            bw.write(re + "\n");
        }
        bw.close();
    }
}
