package com.xhf.leetcode.plugin.io.http.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.io.console.ConsoleUtils;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.utils.BundleUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;
import org.apache.commons.lang3.StringUtils;
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

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class HttpClient {

    private static final HttpClient instance = new HttpClient();
    private final CookieStore cookieStore = new BasicCookieStore();
    // private final CloseableHttpClient httpClient = HttpClients.createDefault();
    // 更新为带宽松 cookie 策略的 httpClient, 解决leetcode平台返回的cookie, expire time无法识别的警告
    private final CloseableHttpClient httpClient = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD) // 使用宽松的 cookie 解析策略
            .setConnectTimeout(3000) // 设置连接超时时间
            .setSocketTimeout(2000) // 设置读取超时时间
            .build())
        .setDefaultCookieStore(cookieStore) // 使用自定义的 CookieStore
        .build();

    private HttpClient() {
    }

    public static HttpClient getInstance() {
        return instance;
    }

    // 忘了之前为啥把这个方法deprecated了
    // @Deprecated
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    public void setCookies(List<Cookie> cookieList) {
        for (Cookie cookie : cookieList) {
            setCookie(cookie);
        }
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
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            return null;
        }
        if (httpResponse.getBody() != null) {
            LogUtils.info("response body: " + httpResponse.getBody());
        }

        return httpResponse;
    }

    public HttpResponse executeGet(@NotNull HttpRequest httpRequest, @NotNull Project project) {
        HttpResponse httpResponse = executeGet(httpRequest);
        if (httpResponse == null) {
            LogUtils.error("httpResponse is null, possible network error. url = " + httpRequest.getUrl());
            ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("network.error"), false, true);
            throw new RuntimeException("network exception or request url error!");
        }
        try {
            return loginCheck(httpResponse);
        } catch (LoginException e) {
            ConsoleUtils.getInstance(project).showWaring(e.getMessage(), false, true);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private HttpResponse loginCheck(HttpResponse httpResponse) throws LoginException {
        String body = httpResponse.getBody();
        String error = "";
        // 如果解析失败, 则认为是正常的json数据, 直接返回
        try {
            JsonElement errorEle = JsonParser.parseString(body).getAsJsonObject().get("error");
            // 如果没有error字段, 则认为是正常的json数据, 直接返回
            if (errorEle == null) {
                return httpResponse;
            }
            error = errorEle.getAsString();
        } catch (Exception ignored) {
            return httpResponse;
        }
        if (StringUtils.equals(error, "User is not authenticated")) {
            throw new LoginException(
                BundleUtils.i18nHelper("登录过期, 请重新登陆!", "Login expired, please login again!"));
        }
        return httpResponse;
    }

    /**
     * 添加请求头
     *
     * @param request request
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
                ConsoleUtils.getInstance(project).showWaring(BundleUtils.i18n("network.error"), false, true);
            }
            throw new RuntimeException("network exception or request url error!");
        }
        try {
            return loginCheck(httpResponse);
        } catch (LoginException e) {
            ConsoleUtils.getInstance(project).showWaring(e.getMessage(), false, true);
            throw new RuntimeException(e);
        }
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
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            return null;
        }
        if (httpResponse.getBody() != null) {
            if (httpResponse.getBody().length() < 10000) {
                LogUtils.info("response body: " + httpResponse.getBody());
            } else {
                try {
                    LogUtils.info(
                        "response body is too large. system will record part of it : " + httpResponse.getBody()
                            .substring(0, 1000));
                } catch (Exception e) {
                    LogUtils.error("unknown error happened! ", e);
                }
            }
        }

        return httpResponse;
    }

    public void clearCookies() {
        cookieStore.clear();
    }

    public boolean containsCookie(String key) {
        return cookieStore.getCookies().stream()
            .anyMatch(cookie -> cookie.getName().equals(key));
    }

}