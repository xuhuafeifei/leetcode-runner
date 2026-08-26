package com.xhf.leetcode.plugin.io.http;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.ArrayList;
import java.util.List;

/**
 * Apache CookieStore 和 OkHttp CookieJar 之间的桥接
 * 用于迁移过程中共享 cookie
 */
public class ApacheCookieJarBridge implements CookieJar {

    private final CookieStore apacheCookieStore;

    public ApacheCookieJarBridge(CookieStore apacheCookieStore) {
        this.apacheCookieStore = apacheCookieStore;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie okCookie : cookies) {
            BasicClientCookie2 apacheCookie = new BasicClientCookie2(okCookie.name(), okCookie.value());
            apacheCookie.setDomain(okCookie.domain());
            apacheCookie.setPath(okCookie.path());
            apacheCookie.setSecure(okCookie.secure());
            apacheCookie.setExpiryDate(new java.util.Date(okCookie.expiresAt()));
            apacheCookieStore.addCookie(apacheCookie);
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        List<org.apache.http.cookie.Cookie> apacheCookies = apacheCookieStore.getCookies();
        for (org.apache.http.cookie.Cookie apacheCookie : apacheCookies) {
            // 简单过滤：只返回匹配域名的 cookie
            Cookie.Builder builder = new Cookie.Builder()
                .name(apacheCookie.getName())
                .value(apacheCookie.getValue() != null ? apacheCookie.getValue() : "")
                .domain(apacheCookie.getDomain() != null ? apacheCookie.getDomain() : url.host())
                .path(apacheCookie.getPath() != null ? apacheCookie.getPath() : "/");

            if (apacheCookie.isSecure()) {
                builder.secure();
            }

            result.add(builder.build());
        }
        return result;
    }
}