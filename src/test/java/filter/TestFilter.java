package filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.window.filter.DifficultyFilter;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import com.xhf.leetcode.plugin.window.filter.QFilterChain;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class TestFilter {
    private LeetcodeClient client = LeetcodeClient.getInstanceForTest();

    @Test
    public void testFilter() {
        List<Question> questionList = client.getQuestionList(new GraphqlReqBody.SearchParams
                .ParamsBuilder()
                .basicParams()
                .build()
        );
        System.out.println(questionList.size());
        /**
         * EASY
         * MEDIUM
         * HARD
         */
        Filter<Question, String> filter = new DifficultyFilter().addItem("EASY");
        List<Question> apply = new QFilterChain().addFilter(filter).apply(questionList);
        System.out.println(apply.size());
        int[] ans = new int[]{1,9,13,14,20,21,26,27,28,35};
        for (int i = 0; i < 10; i++) {
            assert Integer.parseInt(apply.get(i).getFrontendQuestionId()) == ans[i];
        }
    }

    @Test
    public void testJSQuestion() {
        List<Question> totalQuestion = client.queryTotalQuestion();
        Stream<Question> x = totalQuestion.stream().filter(e -> e.getTopicTags().size() == 0);
        List<Question> collect = x.collect(Collectors.toList());
        for (Question question : collect) {
            System.out.println(question.toString());
        }
    }

    @Test
    public void testJSQuestion2() throws Exception {
        // 手动获取js题目的id
        StringBuilder contentBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(new File("E:\\java_code\\leetcode-runner\\src\\test\\java\\filter\\jsQ.json"));
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = contentBuilder.toString();

        List<Question> questions = GsonUtils.fromJsonToList(json, Question.class);
        for (Question question : questions) {
            String fid = question.getFrontendQuestionId();
            System.out.print("\"" + fid + "\"" + ",");
        }
    }


    private final static List<String> options = Arrays.asList("array","string","sorting","matrix","simulation","enumeration","string-matching","counting-sort","bucket-sort","radix-sort","dynamic-programming","greedy","depth-first-search","binary-search","breadth-first-search","backtracking","recursion","divide-and-conquer","memoization","merge-sort","quickselect","hash-table","tree","binary-tree","heap-priority-queue","stack","graph","linked-list","monotonic-stack","ordered-set","queue","binary-search-tree","topological-sort","shortest-path","monotonic-queue","doubly-linked-list","minimum-spanning-tree","strongly-connected-component","eulerian-circuit","biconnected-component","union-find","trie","segment-tree","binary-indexed-tree","suffix-array","bit-manipulation","two-pointers","prefix-sum","counting","sliding-window","bitmask","hash-function","rolling-hash","line-sweep","math","number-theory","geometry","combinatorics","game-theory","randomized","probability-and-statistics","reservoir-sampling","rejection-sampling","database","design","data-stream","interactive","brainteaser","iterator","concurrency","shell");
    private final static List<String> converted = Arrays.asList("数组","字符串","排序","矩阵","模拟","枚举","字符串匹配","计数排序","桶排序","基数排序","动态规划","贪心","深度优先搜索","二分查找","广度优先搜索","回溯","递归","分治","记忆化搜索","归并排序","快速选择","哈希表","树","二叉树","堆（优先队列）","栈","图","链表","单调栈","有序集合","队列","二叉搜索树","拓扑排序","最短路","单调队列","双向链表","最小生成树","强连通分量","欧拉回路","双连通分量","并查集","字典树","线段树","树状数组","后缀数组","位运算","双指针","前缀和","计数","滑动窗口","状态压缩","哈希函数","滚动哈希","扫描线","数学","数论","几何","组合数学","博弈","随机化","概率与统计","水塘抽样","拒绝采样","数据库","设计","数据流","交互","脑筋急转弯","迭代器","多线程","");
    /**
     * 手动获取算法标签
     */
    @Test
    public void testTags() {
        // 手动获取js题目的id
        StringBuilder contentBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(new File("E:\\java_code\\leetcode-runner\\src\\test\\java\\filter\\questionTagTypeWithTags.json"));
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = contentBuilder.toString();

        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("questionTagTypeWithTags");
        for (JsonElement element : jsonArray) {
            JsonArray tagRelation = element.getAsJsonObject().getAsJsonArray("tagRelation");
            for (JsonElement ele : tagRelation) {
                JsonElement tag = ele.getAsJsonObject().get("tag");
//                String slug = tag.getAsJsonObject().get("slug").getAsString();
//                System.out.print("\"" + slug + "\"" + ",");
                String nameTranslated = tag.getAsJsonObject().get("nameTranslated").getAsString();
                System.out.print("\"" + nameTranslated + "\"" + ",");
            }
        }
    }
}
