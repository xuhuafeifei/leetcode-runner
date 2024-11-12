package com.xhf.learning.okhttp;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.util.Objects;

public class HttpTester {
    private static OkHttpClient client = new OkHttpClient();

    @Test
    public void httpGet() throws Exception {
        Request request = new Request.Builder()
                .url("https://blog.csdn.net/weixin_64015266/article/details/139069620")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response);
        System.out.println(Objects.requireNonNull(response.body()).toString());
    }

    @Test
    public void httpPost() throws Exception {
        System.out.println(URLUtils.getLeetcodeLogin());
        String loginUrl = URLUtils.getLeetcodeLogin();
        // build request
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "18966174573")
                .addFormDataPart("password", "2003Nian7Yue6")
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(requestBody)
                .build();

        Response response = HttpHandler.sendHttpRequest(request);

        handleResponse(response);
    }

    private void handleResponse(Response response) {
        if (response.isSuccessful()) {
            System.out.println(response.body().toString());
        } else {
            System.out.println("error");
            System.out.println(response);
        }
    }

    @Test
    public void testLeetcodeAll() throws Exception {
        String allUrl = URLUtils.getLeetcodeAll();
        Request request = new Request.Builder()
                .url(allUrl)
                .build();

        Response response = HttpHandler.sendHttpRequest(request);

        handleResponse(response);
    }
}
