package com.xhf.learning.httpclient;

import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.service.QuestionService;
import org.junit.Test;

public class HttpClientTester {
    @Test
    public void testGraphql() {
        GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams.ParamsBuilder()
                .basicParams()
                .setTitleSlug("title-slug")
                .setSearchKeywords("abab")
                .build();
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_CONTENT_QUERY);
        body.setBySearchParams(params);

        System.out.println(body.toJsonStr());
    }

    @Test
    public void testQueryQuestionContent() {
        LeetcodeClient instance = LeetcodeClient.getInstanceForTest();

        GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams.ParamsBuilder()
                .setTitleSlug("find-the-power-of-k-size-subarrays-i")
                .build();

//        Question question = instance.queryQuestionInfo(params);
//        System.out.println(question);
        Question question = new Question();
        question.setTitleSlug("find-the-power-of-k-size-subarrays-i");
        QuestionService.getInstance().fillQuestion(question, null);
        System.out.println(question);
    }
}
