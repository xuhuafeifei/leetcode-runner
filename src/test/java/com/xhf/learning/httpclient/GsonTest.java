package com.xhf.learning.httpclient;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xhf.learning.httpclient.model.GraphqlReqBody;
import com.xhf.learning.httpclient.model.HttpRequest;
import com.xhf.learning.httpclient.model.HttpResponse;
import com.xhf.learning.httpclient.utils.HttpClientUtils;
import com.xhf.learning.httpclient.utils.LeetcodeApiUtils;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.junit.Test;

public class GsonTest {
    Gson gson = new Gson();

    @Test
    public void parseUserStatus() {
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

        String resp = httpResponse.getBody();

        System.out.println(resp);

        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        JsonObject userStatusObject = dataObject.getAsJsonObject("userStatus");

        // 提取 isSignedIn 字段的值
        boolean isSignedIn = userStatusObject.get("isSignedIn").getAsBoolean();

        System.out.println(isSignedIn);
    }
}
