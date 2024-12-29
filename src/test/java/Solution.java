/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Solution {
    static class Demo {
        private int a;
        private int b;

        public int eval(int x, int y) {
            return x + y;
        }
    }
    public String test(Integer a, Float b, boolean c, Demo d) {
        a = 10;
        b = 20f;
        c = false;
        return "a=" + a + ",b=" + b + ",c=" + c + ",d=" + d;
    }
}
