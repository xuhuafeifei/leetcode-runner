package com.xhf.leetcode.plugin.model;

import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author feigebuge
 */
public class HttpRequest {

    private String url;

    private String body;
    /**
     * POST
     */
    private String contentType;

    private Map<String, String> Header = new HashMap<>();

    public static HttpRequest get(String url) {
        return new HttpRequest(url, null);
    }

    public static HttpRequest post(String url, String contentType) {
        return new HttpRequest(url, contentType);
    }

    public static HttpRequest put(String url, String contentType) {
        return new HttpRequest(url, contentType);
    }

    private HttpRequest(String url, String contentType) {
        this.url = url;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void addHeader(String name, String value) {
        Header.put(name, value);
    }

    public Map<String, String> getHeader() {
        return Header;
    }

    public void addParam(String key, String value) throws UnsupportedEncodingException {
        if (body == null || body.isEmpty()) {
            body = key + "=" + value;
        } else {
            body = body + "&" + key + "=" +value;
        }
    }

    public static class RequestBuilder {
        private String url;

        private String contentType;

        private Map<String, String> jsonBody = new HashMap<>();

        private Map<String, String> Header = new HashMap<>();

        private String body;

        public RequestBuilder(String url) {
            this.url = url;
        }

        public RequestBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public RequestBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public RequestBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public RequestBuilder addHeader(String key, String value) {
            this.Header.put(key, value);
            return this;
        }

        public RequestBuilder addJsonBody(String key, String value) {
            this.jsonBody.put(key, value);
            return this;
        }

        public RequestBuilder addBasicHeader() {
            addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0");
            addHeader("Connection", "keep-alive");
            addHeader("Accept", "*/*");
            addHeader("Host", LeetcodeApiUtils.getLeetcodeHost());
            return this;
        }

        public HttpRequest build() {
            HttpRequest httpRequest = new HttpRequest(this.url, this.contentType);
            httpRequest.Header = this.Header;
            httpRequest.body = this.body;
            return httpRequest;
        }

    }
}