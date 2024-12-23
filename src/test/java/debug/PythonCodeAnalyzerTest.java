package debug;

import com.xhf.leetcode.plugin.debug.analysis.analyzer.PythonCodeAnalyzer;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class PythonCodeAnalyzerTest {
    @Test
    public void test1() {
        PythonCodeAnalyzer an = new PythonCodeAnalyzer(null);

        System.out.println(an.analyze("def minAnagramLength(self, s: str) -> int:"));
        System.out.println(an.analyze("def maxArea(self, height: List[int]) -> int:"));
        System.out.println(an.analyze("def removeNthFromEnd(self, head: Optional[ListNode], n: int) -> Optional[ListNode]:"));
        System.out.println(an.analyze("def removeElement(self, nums: List[int], val: int) -> int:"));
        System.out.println(an.analyze("def nextPermutation(self, nums: List[int]) -> None:"));
        System.out.println(an.analyze("def isValidSudoku(self, board: List[List[str]]) -> bool:"));
        System.out.println(an.analyze("def combinationSum(self, candidates: List[int], target: int) -> List[List[int]]:"));
        System.out.println(an.analyze("def combinationSum(self, candidates: List[int], target: int, a:  List[List[str]], b: float, c: Boolean) -> List[List[int]]:"));
    }
}
