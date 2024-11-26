package com.xhf.leetcode.plugin.io.http;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Urls;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.ide.HttpRequestHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
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

    @Override
    public boolean isSupported(@NotNull FullHttpRequest request) {
        return (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD || request.method() == HttpMethod.POST) && request.uri().startsWith(PREFIX);
    }

    @Override
    public boolean process(@NotNull QueryStringDecoder urlDecoder,
                           @NotNull FullHttpRequest request,
                           @NotNull ChannelHandlerContext context) throws IOException {
        resourcesController.process(urlDecoder, request, context);
        return true;
    }
}