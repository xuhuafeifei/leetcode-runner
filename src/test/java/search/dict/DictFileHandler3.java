package search.dict;

import org.junit.Test;

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
public class DictFileHandler3 {

    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle2.dic");
        BufferedReader br = new BufferedReader(fr);

        List<String> res = new ArrayList<>(2500);

        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split("、|-|/");
            for (String s : split) {
                res.add(s);
            }
        }

        FileWriter fw = new FileWriter("E:\\java_code\\leetcode-runner\\src\\main\\resources\\dict\\handle3.dic");
        BufferedWriter bw = new BufferedWriter(fw);

        for (String re : res) {
            bw.write((re + "\n"));
        }

        bw.close();
    }
}
