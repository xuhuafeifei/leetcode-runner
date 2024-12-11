package com.xhf.leetcode.plugin.io.http;

import com.intellij.openapi.util.io.FileUtilRt;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.FileResponses;
import org.jetbrains.io.Responses;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 处理http请求, 返回当前路径下的vditor框架文件
 * <p>
 * 当前类服务于{@link com.xhf.leetcode.plugin.editors.MarkDownEditor}. 并在{@link LocalHttpRequestHandler}中调用
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LocalResourceController {

    protected static final long LAST_MODIFIED = System.currentTimeMillis();
    private final String controllerPath = "resources";

    // every time the plugin starts up, assume resources could have been modified
    public final void process(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) throws IOException {
        FullHttpResponse response;
        try {
            if (request.method() == HttpMethod.POST) {
                response = post(urlDecoder, request, context);
            } else if (request.method() == HttpMethod.GET) {
                response = get(urlDecoder, request, context);
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            }
        } catch (Throwable t) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(t.getMessage().getBytes(StandardCharsets.UTF_8)));
        }
        Responses.send(response, context.channel(), request);
        if (response.content() != Unpooled.EMPTY_BUFFER) {
            try {
                response.release();
            } catch (Exception ignore) {
            }
        }
    }

    protected String getResourceName(QueryStringDecoder urlDecoder) {
        return "/template/" + urlDecoder.path().substring(LocalHttpRequestHandler.PREFIX.length());
    }

    private FullHttpResponse get(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) {
        String resourceName = getResourceName(urlDecoder);
        byte[] data;
        try (InputStream inputStream = LocalHttpRequestHandler.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);
            }
            data = FileUtilRt.loadBytes(inputStream);
        } catch (IOException e) {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, FileResponses.INSTANCE.getContentType(resourceName) + "; charset=utf-8");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=3600, public");
        response.headers().set(HttpHeaderNames.ETAG, Long.toString(LAST_MODIFIED));
        return response;
    }

    public FullHttpResponse post(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) throws IOException {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
    }
}
