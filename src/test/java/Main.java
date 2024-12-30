import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * args: paramsSize T1 T2... inputSize i1 i2...
 */
public class Main {
    public static void main(String[] args) {
        Solution.Demo demo = new Solution.Demo();
        Float b = 2F;
        Integer a= 10; Double dou = 0.0; Boolean bo = true;boolean c = true;
        Solution solutionTest = new Solution();
        String demoContent = "jiejiejei";
        String demo1 = solutionTest.demo(demoContent);
        String test = solutionTest.test(a, b, c, demo);
        System.out.println("abab");
        System.out.println("abab");
        System.out.println(test);
        String demo2 = solutionTest.demo(demoContent);
        System.out.println(demo1);
    }
}
