package com.xhf.learning.httpclient;

import com.xhf.leetcode.plugin.io.http.utils.HttpClient;

public class LeetcodeClient {

//    private static final LeetcodeClient lcClient = new LeetcodeClient();
//
//    public LeetcodeClient getInstance() {
//        return lcClient;
//    }

    static HttpClient httpClient = HttpClient.getInstance();

//    public static Boolean isLogin() {
//
//        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
//        // 构建graphql req
//        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.USER_STATUS_QUERY);
//
//        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
//                .setBody(body.toJsonStr())
//                .setContentType("application/json")
//                .addBasicHeader()
//                .build();
//
//        HttpResponse httpResponse = httpClient.executePost(httpRequest);
//
//        String resp = httpResponse.getBody();
//
//        // 提取字段
//        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
//        JsonObject dataObject = jsonObject.getAsJsonObject("data");
//        JsonObject userStatusObject = dataObject.getAsJsonObject("userStatus");
//
//        // 提取 isSignedIn 字段的值
//        return userStatusObject.get("isSignedIn").getAsBoolean();
//    }
//
//    @Deprecated // leetcode 登录接口不适用
//    public Boolean login(String username, String password) {
//        String url = URLUtils.getLeetcodeLogin();
//
//        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
//                .addJsonBody("username", username)
//                .addJsonBody("password", password)
//                .build();
//        HttpResponse httpResponse = httpClient.executePost(httpRequest);
//        return httpResponse.getStatusCode() == 200 ? Boolean.TRUE : Boolean.FALSE;
//    }
}
