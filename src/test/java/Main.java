import java.util.*;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        int x = in.nextInt();
        int maxd = Integer.MAX_VALUE;
        Map<Integer, Map<Integer, Integer>> g = new HashMap<>();
        // init
        for (int i = 0; i < n; ++i) {
            g.put(i, new HashMap<>());
        }
        // build
        for (int i = 0; i < m; ++i) {
            int u = in.nextInt();
            int v = in.nextInt();
            int cost = in.nextInt();
            g.get(u).put(v, cost);
        }
        // min dist
        int[] distTo = new int[n];
        for (int i = 0; i < n; ++i) {
            distTo[i] = maxd;
        }
        distTo[x] = 0;
        // dijiestela
        for (int i = 0; i < n; ++i) {
            // get child
            Map<Integer, Integer> childs = g.get(i);
            Set<Map.Entry<Integer, Integer>> entries = childs.entrySet();
            for (Map.Entry<Integer, Integer> entry : entries) {
            }
        }
    }
}










