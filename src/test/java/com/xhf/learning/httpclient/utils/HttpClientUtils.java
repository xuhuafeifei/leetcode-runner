package com.xhf.learning.httpclient.utils;

import com.xhf.learning.httpclient.model.HttpRequest;
import com.xhf.learning.httpclient.model.HttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpClientUtils {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    private static final CookieStore cookieStore = new BasicCookieStore();

    public static void setCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * 发送GET请求
     *
     * @param httpRequest 封装的请求
     * @return 响应体字符串
     */
    public static HttpResponse executeGet(HttpRequest httpRequest) {
        HttpGet request = new HttpGet(httpRequest.getUrl());

        HttpResponse httpResponse = new HttpResponse(-1);

        addHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(entity));
                for (Header header : response.getAllHeaders()) {
                    httpResponse.addHeader(header.getName(), header.getValue());
                }
            }
        } catch (IOException e) {
            // todo: 修改为弹窗提示
            throw new RuntimeException("GET request failed: " + e.getMessage(), e);
        }

        return httpResponse;
    }

    /**
     * 添加请求头
     * @param request
     */
    private static void addHeaders(HttpRequestBase request) {
        for (Cookie cookie : cookieStore.getCookies()) {
            request.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
    }

    /**
     * 发送POST请求
     *
     * @param httpRequest 封装的请求
     * @return 响应体字符串
     */
    public static HttpResponse executePost(HttpRequest httpRequest) {
        HttpPost request = new HttpPost(httpRequest.getUrl());
        Map<String, String> headers = httpRequest.getHeader();
        String body = httpRequest.getBody();

        addHeaders(request);

        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        // 设置请求体
        if (body != null) {
            try {
                StringEntity stringEntity = new StringEntity(body);
                stringEntity.setContentType(httpRequest.getContentType());
                request.setEntity(stringEntity);
            } catch (IOException e) {
                // todo: 修改为弹窗提示
                throw new RuntimeException("http client set entity failed: " + e.getMessage(), e);
            }
        }

        HttpResponse httpResponse = new HttpResponse(-1);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(entity));
                for (Header header : response.getAllHeaders()) {
                    httpResponse.addHeader(header.getName(), header.getValue());
                }
            }
        } catch (IOException e) {
            // todo: 修改为弹窗提示
            throw new RuntimeException("POST request failed: " + e.getMessage(), e);
        }

        return httpResponse;
    }
}