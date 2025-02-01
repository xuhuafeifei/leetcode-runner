package debug;

import com.xhf.leetcode.plugin.debug.analysis.analyzer.AnalysisResult;
import com.xhf.leetcode.plugin.debug.analysis.analyzer.JavaCodeAnalyzer;
import com.xhf.leetcode.plugin.debug.analysis.converter.JavaTestcaseConvertor;
import com.xhf.leetcode.plugin.debug.execute.ExecuteResult;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class AnalysisTester {
    @Test
    public void test1() {
        JavaCodeAnalyzer jca = new JavaCodeAnalyzer();

        AnalysisResult result = jca.analyze("class Solution {\n" +
                "    public int[] closestRoom(int[] rooms, String queries) {\n" +
                "        \n" +
                "    }\n" +
                "}");

        JavaTestcaseConvertor convertor = new JavaTestcaseConvertor("solution", result);
        String cases = "[[2,2],[1,2],[3,2]]\n" +
                "[[3,1],[3,3],[5,2]]";
        String convert = convertor.convert(cases.split("\n"));
        System.out.println(convert);
    }

    @Test
    public void test2() {
        JavaCodeAnalyzer jca = new JavaCodeAnalyzer();

        AnalysisResult result = jca.analyze("public List<List<Integer>> fourSum(int[] nums, int target) {");

        AnalysisResult result1 = jca.analyze("class Solution {\n" +
                "    public int[] twoSum(int[] nums, int target) {\n" +
                "        \n" +
                "    }\n" +
                "}");
        AnalysisResult result2 = jca.analyze("class Solution {\n" +
                "    public boolean isPalindrome(int x) {\n" +
                "        \n" +
                "    }\n" +
                "}");
        AnalysisResult result3 = jca.analyze("class Solution {\n" +
                "    public String longestCommonPrefix(String[] strs) {\n" +
                "        \n" +
                "    }\n" +
                "}");
        AnalysisResult result4 = jca.analyze("public void abab()");
    }

    @Test
    public void test27() {
        String s = "{\n" +
                "    \"add_line\": 0,\n" +
                "    \"class_name\": \"\",\n" +
                "    \"context\": \"\",\n" +
                "    \"has_result\": false,\n" +
                "    \"method_name\": \"\",\n" +
                "    \"msg\": \"\",\n" +
                "    \"result\": \"\",\n" +
                "    \"success\": false\n" +
                "}";
        var a = GsonUtils.fromJson(s, ExecuteResult.class);
        System.out.println(a);
    }
}
