package debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * args: paramsSize T1 T2... inputSize i1 i2...
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Main {
    public static void main(String[] args) {
        List<List<Integer>> ab = new ArrayList<>();
        ab.add(Arrays.asList(1,2,3));
        String a = "jflda\n\r";
        String trim = a.trim();
        System.out.println(trim);
    }
}
