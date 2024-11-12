package com.xhf.learning.httpclient;

import com.xhf.learning.httpclient.model.HttpRequest;
import com.xhf.learning.httpclient.model.HttpResponse;
import com.xhf.learning.httpclient.utils.HttpClientUtils;
import com.xhf.learning.okhttp.URLUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.junit.Test;

public class HttpClientTester {
    @Test
    public void testUserStatus() {
        String url = URLUtils.getLeetcodePoints();

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url).build();

        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "6ohHzsLWPHqzOmGjEdXWY3Wof1WLXEbITnTFaU1XvQUPJzk6o1PRwVCOLFfCa4Zu"));
        HttpClientUtils.setCookie(new BasicClientCookie2("sl-session", "CcSkPukcLGeJ6P8fFfT+Lg=="));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE0NC4yNTMiLCJfdGltZXN0YW1wIjoxNzMwNzkzNTI2LjYzMTI0MzcsImV4cGlyZWRfdGltZV8iOjE3MzMzMzg4MDAsInZlcnNpb25fa2V5XyI6MiwibGF0ZXN0X3RpbWVzdGFtcF8iOjE3MzA4NTg4OTV9.9LjlUKGo3Ypnw3boIG8TjScH-MZ4QP7FfcWQnbL08fQ"));

        HttpResponse httpResponse = HttpClientUtils.executeGet(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    @Test
    public void testLeetcodeAll() {
        String url = URLUtils.getLeetcodeAll();
        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url).build();

        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ngmnPfcdPZpTbbQQpLjX9UsEjY6uZTpwzd1RNnTZvNOCq9vIL2vc6qbcpkGmCisk"));
        HttpClientUtils.setCookie(new BasicClientCookie2("sl-session", "CcSkPukcLGeJ6P8fFfT+Lg=="));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE0NC4yNTMiLCJfdGltZXN0YW1wIjoxNzMwNzkzNTI2LjYzMTI0MzcsImV4cGlyZWRfdGltZV8iOjE3MzMzMzg4MDAsInZlcnNpb25fa2V5XyI6MiwibGF0ZXN0X3RpbWVzdGFtcF8iOjE3MzA4NTg4OTV9.9LjlUKGo3Ypnw3boIG8TjScH-MZ4QP7FfcWQnbL08fQ"));

        HttpResponse httpResponse = HttpClientUtils.executeGet(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    /**
     csrftoken=ngmnPfcdPZpTbbQQpLjX9UsEjY6uZTpwzd1RNnTZvNOCq9vIL2vc6qbcpkGmCisk; Domain=.leetcode.cn; expires=Wed, 05 Nov 2025 01:50:33 GMT; Max-Age=31449600; Path=/; SameSite=Lax; Secure
     sl-session=CcSkPukcLGeJ6P8fFfT+Lg==; SameSite=None; Secure; Path=/; Max-Age=86400; HttpOnly
     */
    @Test
    public void testLeetcodeLogin() {
        String url = URLUtils.getLeetcodeLogin();

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .addJsonBody("username", "18966174573")
                .addJsonBody("password", "2003Nian7Yue6")
                .build();
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getStatusCode());
        System.out.println(httpResponse.getBody());
        System.out.println(httpRequest.getBody());
    }

    // 测试BasicCookieStore
    @Test
    public void testBasicCookieStore() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        cookieStore.addCookie(new BasicClientCookie2("csrftoken", "ngmnPfcdPZpTbbQQpLjX9UsEjY6uZTpwzd1RNnTZvNOCq9vIL2vc6qbcpkGmCisk"));
        cookieStore.addCookie(new BasicClientCookie2("sl-session", "CcSkPukcLGeJ6P8fFfT+Lg=="));

        HttpClientUtils.setCookie(new BasicClientCookie2("csrftoken", "ngmnPfcdPZpTbbQQpLjX9UsEjY6uZTpwzd1RNnTZvNOCq9vIL2vc6qbcpkGmCisk"));
        HttpClientUtils.setCookie(new BasicClientCookie2("sl-session", "CcSkPukcLGeJ6P8fFfT+Lg=="));
        HttpClientUtils.setCookie(new BasicClientCookie2("LEETCODE_SESSION", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfYXV0aF91c2VyX2lkIjoiMzc1NjA5MCIsIl9hdXRoX3VzZXJfYmFja2VuZCI6ImF1dGhlbnRpY2F0aW9uLmF1dGhfYmFja2VuZHMuUGhvbmVBdXRoZW50aWNhdGlvbkJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI3MDBiYmYzZmE4ZDMxM2U1YmFjYTA3Y2I4N2IzYzJhZGM1NzhhMDY3MGQwNWI4MWZkOTVlYWM0YTU3YmJlOWVmIiwiaWQiOjM3NTYwOTAsImVtYWlsIjoiIiwidXNlcm5hbWUiOiJidS1jaHVhbi1uZWkta3UtZCIsInVzZXJfc2x1ZyI6ImJ1LWNodWFuLW5laS1rdS1kIiwiYXZhdGFyIjoiaHR0cHM6Ly9hc3NldHMubGVldGNvZGUuY24vYWxpeXVuLWxjLXVwbG9hZC91c2Vycy9idS1jaHVhbi1uZWkta3UtZC9hdmF0YXJfMTcxMzU4MTU4Ni5wbmciLCJwaG9uZV92ZXJpZmllZCI6dHJ1ZSwiZGV2aWNlX2lkIjoiYWFlMTU4NjMwZjM2ZGU1N2JmMGZmMjdjMGFlMDdmOTciLCJpcCI6IjEwMS43LjE0NC4yNTMiLCJfdGltZXN0YW1wIjoxNzMwNzkzNTI2LjYzMTI0MzcsImV4cGlyZWRfdGltZV8iOjE3MzMzMzg4MDAsInZlcnNpb25fa2V5XyI6MiwibGF0ZXN0X3RpbWVzdGFtcF8iOjE3MzA4NTg4OTV9.9LjlUKGo3Ypnw3boIG8TjScH-MZ4QP7FfcWQnbL08fQ"));

        String url = URLUtils.getLeetcodeProblems() + "?search=3254";
        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .build();
        HttpResponse httpResponse = HttpClientUtils.executePost(httpRequest);

        System.out.println(httpResponse.getBody());
    }

    @Test
    public void testLeetcodeRuncode() {
        String url = URLUtils.getLeetcodeSubmissions();
    }

    /*----------------------------refresh 测试leetcode接口----------------------------------*/

    @Test
    public void testNewLogin() {

    }
}
