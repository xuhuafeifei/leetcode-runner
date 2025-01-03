import java.util.*;//do not modify or remove anything between start-line and end-line
//lc-start-line
class Solution {
    class Demo {
        public String demo(int[] queens, boolean[] d1, boolean[] d2) {
            System.out.println("abab");
            System.out.println(queens.length);
            return "abab";
        }
    }
    public List<List<String>> solveNQueens(int n) {
        List<List<String>> ans = new ArrayList<>();
        int[] queens = new int[n]; // 皇后放在 (r,queens[r])
        boolean[] col = new boolean[n];
        boolean[] diag1 = new boolean[n * 2 - 1];
        boolean[] diag2 = new boolean[n * 2 - 1];
        Demo d = new Demo();
        d.demo(queens, diag1, diag2);
        dfs(0, queens, col, diag1, diag2, ans);
        return ans;
    }

    private void dfs(int r, int[] queens, boolean[] col, boolean[] diag1, boolean[] diag2, List<List<String>> ans) {
        int n = col.length;
        if (r == n) {
            List<String> board = new ArrayList<>(n); // 预分配空间
            for (int c : queens) {
                char[] row = new char[n];
                Arrays.fill(row, '.');
                row[c] = 'Q';
                board.add(new String(row));
            }
            ans.add(board);
            return;
        }
        // 在 (r,c) 放皇后
        for (int c = 0; c < n; c++) {
            int rc = r - c + n - 1;
            if (!col[c] && !diag1[r + c] && !diag2[rc]) { // 判断能否放皇后
                queens[r] = c; // 直接覆盖，无需恢复现场
                col[c] = diag1[r + c] = diag2[rc] = true; // 皇后占用了 c 列和两条斜线
                dfs(r + 1, queens, col, diag1, diag2, ans);
                col[c] = diag1[r + c] = diag2[rc] = false; // 恢复现场
            }
        }
    }
}
//lc-end-line
