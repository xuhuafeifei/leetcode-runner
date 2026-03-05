package com.xhf.leetcode.plugin.io.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.HttpRequestHandler;

/**
 * 拦截idea发出的特定http请求. 具体拦截的逻辑在isSupported中
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LocalHttpRequestHandler extends HttpRequestHandler {

    public static final String PREFIX = "/leetcode-project/";

    private static final LocalResourceController resourcesController = new LocalResourceController();

    @Override
    public boolean isAccessible(@NotNull HttpRequest request) {
        return true;
    }

    /**
     * 满足条件的请求都会被处理. 处理逻辑封装在process
     *
     * @param request 请求
     * @return true表示RequestHandler支持处理当前请求, false则不支持
     */
    @Override
    public boolean isSupported(@NotNull FullHttpRequest request) {
        return (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD
            || request.method() == HttpMethod.POST) && request.uri().startsWith(PREFIX);
    }

    @Override
    public boolean process(@NotNull QueryStringDecoder urlDecoder,
        @NotNull FullHttpRequest request,
        @NotNull ChannelHandlerContext context) throws IOException {
        resourcesController.process(urlDecoder, request, context);
        return true;
    }
}