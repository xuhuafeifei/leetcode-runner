package com.xhf.leetcode.plugin.io.http.utils;

import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class HttpClient {

    private HttpClient() {}

    private static HttpClient instance = new HttpClient();

    public static HttpClient getInstance() {
        return instance;
    }

    private final CookieStore cookieStore = new BasicCookieStore();

    // private final CloseableHttpClient httpClient = HttpClients.createDefault();
    // 更新为带宽松 cookie 策略的 httpClient, 解决leetcode平台返回的cookie, expire time无法识别的警告
    private final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD) // 使用宽松的 cookie 解析策略
                    .build())
            .setDefaultCookieStore(cookieStore) // 使用自定义的 CookieStore
            .build();

    // 忘了之前为啥把这个方法deprecated了
    // @Deprecated
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    public void setCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * 发送GET请求
     *
     * @param httpRequest 封装的请求
     * @return 响应体字符串
     */
    public HttpResponse executeGet(@NotNull HttpRequest httpRequest) {
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

    public HttpResponse executeGet(@NotNull HttpRequest httpRequest, @NotNull Project project) {
        HttpResponse httpResponse = executeGet(httpRequest);
        if (httpResponse == null) {
            LogUtils.error("httpResponse is null, possible network error. url = " + httpRequest.getUrl());
            ConsoleUtils.getInstance(project).showWaring("request failed, possible network error! please check your network !", false, true);
            throw new RuntimeException("network exception or request url error!");
        }
        return httpResponse;
    }

    /**
     * 添加请求头
     * @param request
     */
    private void addHeaders(HttpRequestBase request) {
        for (Cookie cookie : cookieStore.getCookies()) {
            request.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
    }

    public HttpResponse executePost(@NotNull HttpRequest httpRequest, Project project) {
        HttpResponse httpResponse = executePost(httpRequest);
        if (httpResponse == null) {
            LogUtils.error("httpResponse is null, possible network error. url = " + httpRequest.getUrl());
            if (project != null) {
                ConsoleUtils.getInstance(project).showWaring("request failed, possible network error! please check your network !", false, true);
            }
            throw new RuntimeException("network exception or request url error!");
        }
        return httpResponse;
    }

    /**
     * 发送POST请求
     *
     * @param httpRequest 封装的请求
     * @return 响应体字符串
     */
    public HttpResponse executePost(@NotNull HttpRequest httpRequest) {
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
            StringEntity stringEntity = new StringEntity(body, StandardCharsets.UTF_8);
            stringEntity.setContentType(httpRequest.getContentType());
            request.setEntity(stringEntity);
        }

        HttpResponse httpResponse = new HttpResponse(-1);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(entity, StandardCharsets.UTF_8));
                httpResponse.setMsg(response.getStatusLine().getReasonPhrase());
                for (Header header : response.getAllHeaders()) {
                    httpResponse.addHeader(header.getName(), header.getValue());
                }
            }
        } catch (IOException e) {
            // todo: 修改为弹窗提示
            // throw new RuntimeException("POST request failed: " + e.getMessage(), e);
            LogUtils.info(e.toString());
            return null;
        }

        return httpResponse;
    }

    public void setCookies(List<Cookie> cookieList) {
        for (Cookie cookie : cookieList) {
            setCookie(cookie);
        }
    }

    public void clearCookies() {
        cookieStore.clear();
    }

    public boolean containsCookie(String key) {
        return cookieStore.getCookies().stream()
                .anyMatch(cookie -> cookie.getName().equals(key));
    }

}