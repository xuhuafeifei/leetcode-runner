package com.xhf.learning.httpclient;

import com.xhf.learning.httpclient.model.GraphqlReqBody;
import com.xhf.learning.httpclient.model.HttpRequest;
import com.xhf.learning.httpclient.model.HttpResponse;
import com.xhf.learning.httpclient.utils.HttpClientUtils;
import com.xhf.learning.httpclient.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

public class HttpClientTester2 {
    @Test
    public void testUserStatus() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // 构建graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.USER_STATUS_QUERY);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ckFKYLa4wCSxqJa7JH5mJT5ZMgIxHUychW0tlNXdzPEmVBf7meRogetREtw9GI5j"));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE2OC4xNjEiLCJfdGltZXN0YW1wIjoxNzMwODc1Nzg1LjI2NDkxNTcsImV4cGlyZWRfdGltZV8iOjE3MzM0MjUyMDAsInZlcnNpb25fa2V5XyI6Mn0.NJ16mdmEMio-J3M13qxdA7fRXbDM2B5_OinrncF-Pwc"));
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    @Test
    public void testQuestionSet() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();

        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.PROBLEM_SET_QUERY);
        body.addVariable("categorySlug", "all-code-essentials");
        body.addVariable("skip", 0);
        body.addVariable("limit", 50);
        body.addVariable("filter", new HashMap<String, Object>());

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();


        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ckFKYLa4wCSxqJa7JH5mJT5ZMgIxHUychW0tlNXdzPEmVBf7meRogetREtw9GI5j"));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE2OC4xNjEiLCJfdGltZXN0YW1wIjoxNzMwODc1Nzg1LjI2NDkxNTcsImV4cGlyZWRfdGltZV8iOjE3MzM0MjUyMDAsInZlcnNpb25fa2V5XyI6Mn0.NJ16mdmEMio-J3M13qxdA7fRXbDM2B5_OinrncF-Pwc"));
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    @Test
    public void testQuesionData() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();

        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_DATA_QUERY);
        body.setOperationNames("questionData");
        body.addVariable("titleSlug", "find-the-power-of-k-size-subarrays-i");

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();


        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ckFKYLa4wCSxqJa7JH5mJT5ZMgIxHUychW0tlNXdzPEmVBf7meRogetREtw9GI5j"));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE2OC4xNjEiLCJfdGltZXN0YW1wIjoxNzMwODc1Nzg1LjI2NDkxNTcsImV4cGlyZWRfdGltZV8iOjE3MzM0MjUyMDAsInZlcnNpb25fa2V5XyI6Mn0.NJ16mdmEMio-J3M13qxdA7fRXbDM2B5_OinrncF-Pwc"));
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    @Test
    public void testQuestionStatus() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();

        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_STATUS_QUERY);
        body.setOperationNames("allQuestionsStatuses");

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();


        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ckFKYLa4wCSxqJa7JH5mJT5ZMgIxHUychW0tlNXdzPEmVBf7meRogetREtw9GI5j"));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE2OC4xNjEiLCJfdGltZXN0YW1wIjoxNzMwODc1Nzg1LjI2NDkxNTcsImV4cGlyZWRfdGltZV8iOjE3MzM0MjUyMDAsInZlcnNpb25fa2V5XyI6Mn0.NJ16mdmEMio-J3M13qxdA7fRXbDM2B5_OinrncF-Pwc"));
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getBody());
    }

//    @Test
//    public void testLeetcodeClient() {
//        List<Question> questionList = LeetcodeClient.getQuestionList(new SearchParams
//                .ParamsBuilder()
//                .basicParams()
//                .build()
//        );
//
//        System.out.println(questionList.size());
//        for (Question question : questionList) {
//            System.out.println(question);
//        }
//    }

    @Test
    public void test() {
        String json = "<p>给你一个长度为 <code>n</code>&nbsp;的整数数组&nbsp;<code>nums</code>&nbsp;和一个正整数&nbsp;<code>k</code>&nbsp;。</p>\n\n<p>一个数组的 <strong>能量值</strong> 定义为：</p>\n\n<ul>\n\t<li>如果 <strong>所有</strong>&nbsp;元素都是依次&nbsp;<strong>连续</strong> 且 <strong>上升</strong> 的，那么能量值为 <strong>最大</strong>&nbsp;的元素。</li>\n\t<li>否则为 -1 。</li>\n</ul>\n\n<p>你需要求出 <code>nums</code>&nbsp;中所有长度为 <code>k</code>&nbsp;的&nbsp;<span data-keyword=\"subarray-nonempty\">子数组</span>&nbsp;的能量值。</p>\n\n<p>请你返回一个长度为 <code>n - k + 1</code>&nbsp;的整数数组&nbsp;<code>results</code>&nbsp;，其中&nbsp;<code>results[i]</code>&nbsp;是子数组&nbsp;<code>nums[i..(i + k - 1)]</code>&nbsp;的能量值。</p>\n\n<p>&nbsp;</p>\n\n<p><strong class=\"example\">示例 1：</strong></p>\n\n<div class=\"example-block\">\n<p><span class=\"example-io\"><b>输入：</b>nums = [1,2,3,4,3,2,5], k = 3</span></p>\n\n<p><b>输出：</b>[3,4,-1,-1,-1]</p>\n\n<p><strong>解释：</strong></p>\n\n<p><code>nums</code>&nbsp;中总共有 5 个长度为 3 的子数组：</p>\n\n<ul>\n\t<li><code>[1, 2, 3]</code>&nbsp;中最大元素为 3 。</li>\n\t<li><code>[2, 3, 4]</code>&nbsp;中最大元素为 4 。</li>\n\t<li><code>[3, 4, 3]</code>&nbsp;中元素 <strong>不是</strong>&nbsp;连续的。</li>\n\t<li><code>[4, 3, 2]</code>&nbsp;中元素 <b>不是</b>&nbsp;上升的。</li>\n\t<li><code>[3, 2, 5]</code>&nbsp;中元素 <strong>不是</strong>&nbsp;连续的。</li>\n</ul>\n</div>\n\n<p><strong class=\"example\">示例 2：</strong></p>\n\n<div class=\"example-block\">\n<p><span class=\"example-io\"><b>输入：</b>nums = [2,2,2,2,2], k = 4</span></p>\n\n<p><span class=\"example-io\"><b>输出：</b>[-1,-1]</span></p>\n</div>\n\n<p><strong class=\"example\">示例 3：</strong></p>\n\n<div class=\"example-block\">\n<p><span class=\"example-io\"><b>输入：</b>nums = [3,2,3,2,3,2], k = 2</span></p>\n\n<p><span class=\"example-io\"><b>输出：</b>[-1,3,-1,3,-1]</span></p>\n</div>\n\n<p>&nbsp;</p>\n\n<p><strong>提示：</strong></p>\n\n<ul>\n\t<li><code>1 &lt;= n == nums.length &lt;= 500</code></li>\n\t<li><code>1 &lt;= nums[i] &lt;= 10<sup>5</sup></code></li>\n\t<li><code>1 &lt;= k &lt;= n</code></li>\n</ul>\n";
        System.out.println(json.replaceAll("\n\n", ""));
    }

    @Test
    public void test2() {
        Question question = new Question();
        String jsonStr = GsonUtils.toJsonStr(question);
        Question question1 = GsonUtils.fromJson(jsonStr, Question.class);
        System.out.println(question);
        System.out.println(question1);
        System.out.println(question1 == question);
    }

    @Test
    public void test3() throws IOException {
        String path = "E:\\java_code\\leetcode-runner\\src\\main\\resources\\demo.txt";
        String content = "jfadljflkjflkajfdkljsl";
        FileUtils.createAndWriteFile(path, content);
        FileUtils.createAndWriteFile(path, "abababab");
    }
}
