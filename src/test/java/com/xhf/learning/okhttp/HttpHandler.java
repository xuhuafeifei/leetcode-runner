package com.xhf.learning.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpHandler {
    private static OkHttpClient client = new OkHttpClient();

    // 发送请求
    public static Response sendHttpRequest(Request request) {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
