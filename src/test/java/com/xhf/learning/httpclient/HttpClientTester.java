package com.xhf.learning.httpclient;

import com.xhf.leetcode.plugin.io.http.LeetcodeClient;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.service.QuestionService;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.http.impl.cookie.BasicClientCookie2;
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

    static LeetcodeClient instance = LeetcodeClient.getInstanceForTest();

    static {
        HttpClient.getInstance().setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImRqYW5nby5jb250cmliLmF1dGguYmFja2VuZHMuTW9kZWxCYWNrZW5kIiwiX2F1dGhfdXNlcl9oYXNoIjoiNzAwYmJmM2ZhOGQzMTNlNWJhY2EwN2NiODdiM2MyYWRjNTc4YTA2NzBkMDViODFmZDk1ZWFjNGE1N2JiZTllZiIsImlkIjozNzU2MDkwLCJlbWFpbCI6IiIsInVzZXJuYW1lIjoiYnUtY2h1YW4tbmVpLWt1LWQiLCJ1c2VyX3NsdWciOiJidS1jaHVhbi1uZWkta3UtZCIsImF2YXRhciI6Imh0dHBzOi8vYXNzZXRzLmxlZXRjb2RlLmNuL2FsaXl1bi1sYy11cGxvYWQvdXNlcnMvYnUtY2h1YW4tbmVpLWt1LWQvYXZhdGFyXzE3MTM1ODE1ODYucG5nIiwicGhvbmVfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6ImFhZTE1ODYzMGYzNmRlNTdiZjBmZjI3YzBhZTA3Zjk3IiwiaXAiOiIyMTEuOTMuMjQ4LjI0OSIsIl90aW1lc3RhbXAiOjE3MzExNTExOTQuMzE2MTY1LCJleHBpcmVkX3RpbWVfIjoxNzMzNjg0NDAwLCJ2ZXJzaW9uX2tleV8iOjIsImxhdGVzdF90aW1lc3RhbXBfIjoxNzMxNDkzNDA2fQ.5WWDvj8dsEb2XNFPsDMLtVmb_Pj31mJCelJmLcHqiz8; _ga=GA1.1.1419912094.1718548156; a2873925c34ecbd2_gr_cs1=bu-chuan-nei-ku-d"));
    }

    @Test
    public void testRunCode() {
        LeetcodeClient instance = LeetcodeClient.getInstanceForTest();

        HttpClient.getInstance().setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImRqYW5nby5jb250cmliLmF1dGguYmFja2VuZHMuTW9kZWxCYWNrZW5kIiwiX2F1dGhfdXNlcl9oYXNoIjoiNzAwYmJmM2ZhOGQzMTNlNWJhY2EwN2NiODdiM2MyYWRjNTc4YTA2NzBkMDViODFmZDk1ZWFjNGE1N2JiZTllZiIsImlkIjozNzU2MDkwLCJlbWFpbCI6IiIsInVzZXJuYW1lIjoiYnUtY2h1YW4tbmVpLWt1LWQiLCJ1c2VyX3NsdWciOiJidS1jaHVhbi1uZWkta3UtZCIsImF2YXRhciI6Imh0dHBzOi8vYXNzZXRzLmxlZXRjb2RlLmNuL2FsaXl1bi1sYy11cGxvYWQvdXNlcnMvYnUtY2h1YW4tbmVpLWt1LWQvYXZhdGFyXzE3MTM1ODE1ODYucG5nIiwicGhvbmVfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6ImFhZTE1ODYzMGYzNmRlNTdiZjBmZjI3YzBhZTA3Zjk3IiwiaXAiOiIyMTEuOTMuMjQ4LjI0OSIsIl90aW1lc3RhbXAiOjE3MzExNTExOTQuMzE2MTY1LCJleHBpcmVkX3RpbWVfIjoxNzMzNjg0NDAwLCJ2ZXJzaW9uX2tleV8iOjIsImxhdGVzdF90aW1lc3RhbXBfIjoxNzMxNDkzNDA2fQ.5WWDvj8dsEb2XNFPsDMLtVmb_Pj31mJCelJmLcHqiz8; _ga=GA1.1.1419912094.1718548156; a2873925c34ecbd2_gr_cs1=bu-chuan-nei-ku-d"));
//        HttpClient.getInstance().setCookie(new BasicClientCookie2("csrftoken", "6yttTvsyxNFIUIPSHSK6spidCqsmYTfbZ0AxT5tJ0UAnTGnWJ2NhhNSPnBz0MLPk"));

        RunCode runCode = new RunCode();
        runCode.setLang("python3");
        runCode.setQuestionId("3546");
        runCode.setTypeCode("class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = [3]\n" +
                "        # task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans");
        runCode.setDataInput("\"0001111\"\n2\n[[0,6]]\n\"010101\"\n1\n[[0,5],[1,4],[2,3]]");
        runCode.setTitleSlug("count-substrings-that-satisfy-k-constraint-ii");
        RunCodeResult runCodeResult = instance.runCode(runCode);
        System.out.println(GsonUtils.toJsonStr(runCodeResult));
    }

    @Test
    public void testGson() {
        RunCode runCode = new RunCode();
        runCode.setLang("python3");
        runCode.setQuestionId("3546");
        runCode.setTypeCode("class Solution:\n    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n        n = len(s)\n        left = [0] * n\n        pre = [0] * (n + 1)\n        cnt = [0, 0]\n        abab\n        l = 0\n        for i, c in enumerate(s):\n            cnt[ord(c) & 1] += 1\n            while cnt[0] > k and cnt[1] > k:\n                cnt[ord(s[l]) & 1] -= 1\n                l += 1\n            left[i] = l\n            pre[i + 1] = pre[i] + i - l + 1\n\n        right = [0] * n\n        l = 0\n        for i in range(n):\n            while l < n and left[l] < i:\n                l += 1\n            right[i] = l\n\n        ans = []\n        for l, r in queries:\n            j = min(right[l], r + 1)\n            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n        return ans\n");
        runCode.setDataInput("\"0001111\"\n2\n[[0,6]]\n\"010101\"\n1\n[[0,5],[1,4],[2,3]]");
        runCode.setTitleSlug("count-substrings-that-satisfy-k-constraint-ii");

        System.out.println(GsonUtils.toJsonStr(runCode));
    }

    @Test
    public void testRunCodeUnderDiffCodeContent() throws InterruptedException {
        String[] template = new String[]{getSucc(), getResError(), getRuntimeError(), getOutOfMemory(), getOutOfTime()};
        RunCode runCode = new RunCode();
        runCode.setLang("python3");
        runCode.setQuestionId("3546");
        runCode.setDataInput("\"0001111\"\n2\n[[0,6]]\n\"010101\"\n1\n[[0,5],[1,4],[2,3]]");
        runCode.setTitleSlug("count-substrings-that-satisfy-k-constraint-ii");

        for (String t : template) {
            runCode.setTypeCode(t);
            // request
            RunCodeResult runCodeResult = instance.runCode(runCode);
            System.out.println(GsonUtils.toJsonStr(runCodeResult));
            System.out.println();
            Thread.sleep(3000);
        }
    }

    public String getSucc() {
        return "class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = []\n" +
                "        # task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans\n";
    }

    public String getResError() {
        return "class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = [3]\n" +
                "        # task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans\n";
    }

    public String getRuntimeError() {
        return "class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = []\n" +
                "        abab\n" +
                "        # task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans\n";
    }


    public String getOutOfMemory() {
        return "class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = []\n" +
                "        task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans\n";
    }

    public String getOutOfTime() {
        return "class Solution:\n" +
                "    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\n" +
                "        n = len(s)\n" +
                "        left = [0] * n\n" +
                "        pre = [0] * (n + 1)\n" +
                "        cnt = [0, 0]\n" +
                "        l = 0\n" +
                "        for i in range(10 ** 7):\n" +
                "            pass\n" +
                "        for i, c in enumerate(s):\n" +
                "            cnt[ord(c) & 1] += 1\n" +
                "            while cnt[0] > k and cnt[1] > k:\n" +
                "                cnt[ord(s[l]) & 1] -= 1\n" +
                "                l += 1\n" +
                "            left[i] = l\n" +
                "            pre[i + 1] = pre[i] + i - l + 1\n" +
                "\n" +
                "        right = [0] * n\n" +
                "        l = 0\n" +
                "        for i in range(n):\n" +
                "            while l < n and left[l] < i:\n" +
                "                l += 1\n" +
                "            right[i] = l\n" +
                "\n" +
                "        ans = []\n" +
                "        while True:\n" +
                "            a = 1\n" +
                "        # task = [0] * (10 ** 9)\n" +
                "        for l, r in queries:\n" +
                "            j = min(right[l], r + 1)\n" +
                "            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\n" +
                "        return ans\n";
    }


    @Test
    public void testRUnCode2() {
        // String json = "{\"title_slug\":\"two-sum\",\"lang\":\"Java\",\"question_id\":\"1\",\"typed_code\":\"package file;\\n\\nclass Solution {\\n    public int[] twoSum(int[] nums, int target) {\\n        int[] ans \\u003d new int[2];\\n        for (int i \\u003d 0; i \\u003c nums.length; ++i) {\\n            for (int j \\u003d i + 1; j \\u003c nums.length; ++j) {\\n                if (nums[i] + nums[j] \\u003d\\u003d target) {\\n                    ans[0] \\u003d i;\\n                    ans[1] \\u003d j;\\n                    return ans;\\n                }\\n            }\\n        }\\n        return ans;\\n    }\\n}\",\"data_input\":\"[2,7,11,15]\\n9\\n[3,2,4]\\n6\\n[3,3]\\n6\"}";
        // RunCode runCode = GsonUtils.fromJson(json, RunCode.class);
        RunCode runCode = new RunCode();

        runCode.setQuestionId("1");
        runCode.setLang("java");
        runCode.setTitleSlug("two-sum");
        runCode.setDataInput("[2,7,11,15]\n9");
        runCode.setTypeCode("package file;\n" +
                "\n" +
                "class Solution {\n" +
                "    public int[] twoSum(int[] nums, int target) {\n" +
                "        int[] ans = new int[2];\n" +
                "        for (int i = 0; i < nums.length; ++i) {\n" +
                "            for (int j = i + 1; j < nums.length; ++j) {\n" +
                "                if (nums[i] + nums[j] == target) {\n" +
                "                    ans[0] = i;\n" +
                "                    ans[1] = j;\n" +
                "                    return ans;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        return ans;\n" +
                "    }\n" +
                "}");

        RunCodeResult runCodeResult = instance.runCode(runCode);
        System.out.println(GsonUtils.toJsonStr(runCodeResult));
    }

    @Test
    public void testSubmitCodeUnderDiffCodeContent() throws InterruptedException {
        String[] template = new String[]{getSucc(), getResError(), getRuntimeError(), getOutOfMemory(), getOutOfTime()};
        RunCode runCode = new RunCode();
        runCode.setLang("python3");
        runCode.setQuestionId("3546");
        runCode.setDataInput("\"0001111\"\n2\n[[0,6]]\n\"010101\"\n1\n[[0,5],[1,4],[2,3]]");
        runCode.setTitleSlug("count-substrings-that-satisfy-k-constraint-ii");

        for (String t : template) {
            runCode.setTypeCode(t);
            // request
            SubmitCodeResult submitCodeResult = instance.submitCode(runCode);
            System.out.println(GsonUtils.toJsonStr(submitCodeResult));
            System.out.println();
            Thread.sleep(3000);
        }
    }
}
