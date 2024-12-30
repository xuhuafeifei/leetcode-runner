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
    public String test(int x, float y, boolean bo, Demo d) {
        int a = 10;
        float b = 20f;
        boolean c = false;
        demo("abab");
        int m = 9;
//        System.out.println(Math.min(a, m));
        // System.out.println(d.toString());
//        return "a=" + a + ",b=" + b + ",c=" + c + ",d=";// + d;
        StringBuilder sb = new StringBuilder();
        sb.append("a=").append(a);//.append("b=").append(b);
//        sb.append("b=").append(b);
//        String s = sb.toString();
         String s = "a=" + a + ",b=" + b;
        return "abab";
    }
    public String demo(String haha) {
        return "abab, jiejiejei";
    }
}
