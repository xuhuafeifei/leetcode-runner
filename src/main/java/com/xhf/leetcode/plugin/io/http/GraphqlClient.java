package com.xhf.leetcode.plugin.io.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xhf.leetcode.plugin.utils.LogUtils;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LeetCode GraphQL 客户端（OkHttp 实现）
 * 支持重试、超时控制、错误分类
 */
public class GraphqlClient {

    private static final Gson gson = new Gson();
    private static final long CONNECT_TIMEOUT_SECONDS = 10L;
    private static final long READ_TIMEOUT_SECONDS = 30L;
    private static final int MAX_RETRIES = 2;
    private static final long BASE_DELAY_MS = 500L;

    private final OkHttpClient okHttpClient;
    private final String graphqlUrl;
    private final String origin;

    private GraphqlClient(OkHttpClient okHttpClient, String graphqlUrl, String origin) {
        this.okHttpClient = okHttpClient;
        this.graphqlUrl = graphqlUrl;
        this.origin = origin;
    }

    /**
     * 创建默认配置的实例
     */
    public static GraphqlClient create(CookieJar cookieJar, String graphqlUrl, String origin) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .cookieJar(cookieJar);
        return new GraphqlClient(builder.build(), graphqlUrl, origin);
    }

    /**
     * 执行 GraphQL 查询，返回指定类型的对象（自动从 data 路径解析）
     */
    public <T> T query(String query, Map<String, Object> variables, List<String> dataPath, Class<T> clazz) {
        JsonObject jsonObject = queryForJsonObject(query, variables);
        JsonElement element = jsonObject.getAsJsonObject("data");
        for (String path : dataPath) {
            element = element.getAsJsonObject().get(path);
            if (element == null) {
                throw new LcException.Business("返回数据缺少字段: " + path);
            }
        }
        return gson.fromJson(element, clazz);
    }

    /**
     * 执行 GraphQL 查询，返回列表
     */
    public <T> List<T> queryList(String query, Map<String, Object> variables, List<String> dataPath, Class<T> clazz) {
        JsonObject jsonObject = queryForJsonObject(query, variables);
        JsonElement element = jsonObject.getAsJsonObject("data");
        for (String path : dataPath) {
            element = element.getAsJsonObject().get(path);
            if (element == null) {
                throw new LcException.Business("返回数据缺少字段: " + path);
            }
        }
        JsonArray jsonArray = element.getAsJsonArray();
        List<T> list = new ArrayList<>();
        for (JsonElement item : jsonArray) {
            list.add(gson.fromJson(item, clazz));
        }
        return list;
    }

    /**
     * 执行 GraphQL 查询，返回原始 JsonObject（包含 data/errors 的最外层）
     */
    public JsonObject queryForJsonObject(String query, Map<String, Object> variables) {
        String bodyJson = buildJson(query, variables);
        Exception lastException = null;

        // 重试: 第一次 + MAX_RETRIES 次重试
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                long delay = (long) (BASE_DELAY_MS * Math.pow(2, attempt - 1));
                LogUtils.warn("GraphQL 请求第 " + attempt + " 次重试，延迟 " + delay + "ms");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new LcException.Unknown("重试被中断", e);
                }
            }

            try {
                Request request = new Request.Builder()
                    .url(graphqlUrl)
                    .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Accept", "*/*")
                    .addHeader("Origin", origin)
                    .addHeader("Referer", origin)
                    .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    String bodyStr = response.body() != null ? response.body().string() : "";

                    // HTTP 状态码非 2xx
                    if (!response.isSuccessful()) {
                        if (response.code() == 401 || response.code() == 403) {
                            throw new LcException.Unauthorized("HTTP " + response.code() + ": 登录已失效");
                        }
                        throw new LcException.Network("HTTP " + response.code() + ": " + response.message(), null);
                    }

                    JsonObject jsonObject = JsonParser.parseString(bodyStr).getAsJsonObject();

                    // 检查 GraphQL errors
                    if (jsonObject.has("errors") && !jsonObject.get("errors").isJsonNull()) {
                        JsonArray errors = jsonObject.getAsJsonArray("errors");
                        if (errors != null && errors.size() > 0) {
                            JsonObject firstError = errors.get(0).getAsJsonObject();
                            String msg = firstError.has("message") && !firstError.get("message").isJsonNull()
                                ? firstError.get("message").getAsString() : "GraphQL 错误";
                            throw new LcException.Business(msg);
                        }
                    }

                    return jsonObject;
                }
            } catch (LcException e) {
                // 业务错误、未授权不重试
                throw e;
            } catch (IOException e) {
                lastException = e;
                if (!isRetryable(e)) {
                    throw wrapIOException(e);
                }
            } catch (Exception e) {
                lastException = e;
                // 其他异常也重试
            }
        }

        throw wrapIOException(lastException != null ? lastException : new IOException("未知错误"));
    }

    private static String buildJson(String query, Map<String, Object> variables) {
        Map<String, Object> map = new HashMap<>();
        map.put("query", query);
        map.put("variables", variables);
        return gson.toJson(map);
    }

    private static boolean isRetryable(IOException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        return msg.toLowerCase().contains("timeout")
            || msg.toLowerCase().contains("reset")
            || msg.toLowerCase().contains("refused")
            || e instanceof java.net.SocketTimeoutException
            || e instanceof java.net.SocketException;
    }

    private static LcException wrapIOException(Exception e) {
        if (e instanceof java.net.SocketTimeoutException) {
            return new LcException.Timeout(e);
        } else if (e instanceof java.net.UnknownHostException) {
            return new LcException.Network("域名解析失败，请检查网络", e);
        } else if (e instanceof java.net.ConnectException) {
            return new LcException.Network("连接失败，请检查网络", e);
        } else {
            return new LcException.Unknown(e.getMessage() != null ? e.getMessage() : "未知错误", e);
        }
    }
}
