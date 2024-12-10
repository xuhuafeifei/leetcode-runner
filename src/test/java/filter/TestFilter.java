package filter;

import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.window.filter.DifficultyFilter;
import com.xhf.leetcode.plugin.window.filter.Filter;
import com.xhf.leetcode.plugin.window.filter.FilterChain;
import com.xhf.leetcode.plugin.window.filter.QFilterChain;
import org.junit.Test;

import java.util.List;

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
}
