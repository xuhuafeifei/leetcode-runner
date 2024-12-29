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
        Solution.Demo demo = new Solution.Demo();
        Solution solutionTest = new Solution();
        int a= 10;
        int b= 10;
        int c= 10;
        int d= 10;
        String test = solutionTest.test(1, 2.0f, true, demo);
        System.out.println(test);
    }
}
