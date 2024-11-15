package com.xhf.learning.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class LeetCodeSubmit {

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://leetcode.cn/problems/count-substrings-that-satisfy-k-constraint-ii/interpret_solution/");

            // 设置请求头
//            httpPost.setHeader("Host", "leetcode.cn");
            httpPost.setHeader("Cookie", "LEETCODE_SESSION=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImRqYW5nby5jb250cmliLmF1dGguYmFja2VuZHMuTW9kZWxCYWNrZW5kIiwiX2F1dGhfdXNlcl9oYXNoIjoiNzAwYmJmM2ZhOGQzMTNlNWJhY2EwN2NiODdiM2MyYWRjNTc4YTA2NzBkMDViODFmZDk1ZWFjNGE1N2JiZTllZiIsImlkIjozNzU2MDkwLCJlbWFpbCI6IiIsInVzZXJuYW1lIjoiYnUtY2h1YW4tbmVpLWt1LWQiLCJ1c2VyX3NsdWciOiJidS1jaHVhbi1uZWkta3UtZCIsImF2YXRhciI6Imh0dHBzOi8vYXNzZXRzLmxlZXRjb2RlLmNuL2FsaXl1bi1sYy11cGxvYWQvdXNlcnMvYnUtY2h1YW4tbmVpLWt1LWQvYXZhdGFyXzE3MTM1ODE1ODYucG5nIiwicGhvbmVfdmVyaWZpZWQiOnRydWUsImRldmljZV9pZCI6ImFhZTE1ODYzMGYzNmRlNTdiZjBmZjI3YzBhZTA3Zjk3IiwiaXAiOiIyMTEuOTMuMjQ4LjI0OSIsIl90aW1lc3RhbXAiOjE3MzExNTExOTQuMzE2MTY1LCJleHBpcmVkX3RpbWVfIjoxNzMzNjg0NDAwLCJ2ZXJzaW9uX2tleV8iOjIsImxhdGVzdF90aW1lc3RhbXBfIjoxNzMxNDkzNDA2fQ.5WWDvj8dsEb2XNFPsDMLtVmb_Pj31mJCelJmLcHqiz8; _ga=GA1.1.1419912094.1718548156; a2873925c34ecbd2_gr_cs1=bu-chuan-nei-ku-d");

            // 构建请求体
            String jsonBody = "{\r\n" +
                    "    \"lang\": \"python3\",\r\n" +
                    "    \"question_id\": \"3546\",\r\n" +
                    "    \"typed_code\": \"class Solution:\\n    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\\n        n = len(s)\\n        left = [0] * n\\n        pre = [0] * (n + 1)\\n        cnt = [0, 0]\\n        abab\\n        l = 0\\n        for i, c in enumerate(s):\\n            cnt[ord(c) & 1] += 1\\n            while cnt[0] > k and cnt[1] > k:\\n                cnt[ord(s[l]) & 1] -= 1\\n                l += 1\\n            left[i] = l\\n            pre[i + 1] = pre[i] + i - l + 1\\n\\n        right = [0] * n\\n        l = 0\\n        for i in range(n):\\n            while l < n and left[l] < i:\\n                l += 1\\n            right[i] = l\\n\\n        ans = []\\n        for l, r in queries:\\n            j = min(right[l], r + 1)\\n            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\\n        return ans\\n\",\r\n" +
                    "    \"data_input\": \"\\\"0001111\\\"\\n2\\n[[0,6]]\\n\\\"010101\\\"\\n1\\n[[0,5],[1,4],[2,3]]\"\r\n}";

            jsonBody = "{\r\n" +
                    "    \"lang\": \"python3\",\r\n" +
                    "    \"question_id\": \"3546\",\r\n" +
                    "    \"type_code\": \"class Solution:\\n    def countKConstraintSubstrings(self, s: str, k: int, queries: List[List[int]]) -> List[int]:\\n        n = len(s)\\n        left = [0] * n\\n        pre = [0] * (n + 1)\\n        cnt = [0, 0]\\n        abab\\n        l = 0\\n        for i, c in enumerate(s):\\n            cnt[ord(c) & 1] += 1\\n            while cnt[0] > k and cnt[1] > k:\\n                cnt[ord(s[l]) & 1] -= 1\\n                l += 1\\n            left[i] = l\\n            pre[i + 1] = pre[i] + i - l + 1\\n\\n        right = [0] * n\\n        l = 0\\n        for i in range(n):\\n            while l < n and left[l] < i:\\n                l += 1\\n            right[i] = l\\n\\n        ans = []\\n        for l, r in queries:\\n            j = min(right[l], r + 1)\\n            ans.append(pre[r + 1] - pre[j] + (j - l + 1) * (j - l) // 2)\\n        return ans\\n\",\n" +
                    "    \"data_input\": \"\\\"0001111\\\"\\n2\\n[[0,6]]\\n\\\"010101\\\"\\n1\\n[[0,5],[1,4],[2,3]]\"\r\n" +
                    "}";
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            httpPost.setEntity(entity);
//            httpPost.setHeader("Content-Type", "application/json");

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseString = EntityUtils.toString(responseEntity, "UTF-8");
                    System.out.println("Response: " + responseString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}