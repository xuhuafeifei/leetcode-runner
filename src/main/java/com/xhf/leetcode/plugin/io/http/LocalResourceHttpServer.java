package com.xhf.leetcode.plugin.io.http;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xhf.leetcode.plugin.io.file.utils.FileUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * support local resource for webview
 */
@Service(Service.Level.PROJECT)
@Deprecated // no need to start server, because the idea platform will start
public final class LocalResourceHttpServer {

    private final Project project;
    private int port = -1;
    private HttpServer server;

    public LocalResourceHttpServer(Project project) {
        this.project = project;
        try {
            this.startServer();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.error("Start LocalResourceHttpServer Failed", e);
        }
    }

    public static LocalResourceHttpServer getInstance(Project project) {
        return project.getService(LocalResourceHttpServer.class);
    }


    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        this.port = server.getAddress().getPort();
        server.createContext("/", new ResourceHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + this.port);
    }

    public int getPort() {
        if (port == -1) {
            throw new RuntimeException("server start error...");
        }
        return port;
    }


    static class ResourceHandler implements HttpHandler {

        public static void main(String[] args) throws IOException {
            new LocalResourceHttpServer(null);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath().substring(1);

            String resource = new FileUtils.PathBuilder("\\template").append(path).build();
            URL url = getClass().getResource(FileUtils.unUnifyPath(resource));

            if (url == null) {
                send404Response(exchange);
                return;
            }

            try (InputStream is = url.openStream()) {
                URLConnection connection = url.openConnection();
                int contentLength = connection.getContentLength();

                setResponseHeaders(exchange, contentLength, path);

                byte[] buffer = new byte[2048];
                int length;
                OutputStream os = exchange.getResponseBody();
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
            }
        }

        private void send404Response(HttpExchange exchange) throws IOException {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void setResponseHeaders(HttpExchange exchange, int contentLength, String path) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", getContentType(path));

            // 设置 CORS 相关的响应头
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // 允许所有来源
            exchange.getResponseHeaders()
                .add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // 允许的方法
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // 允许的请求头
            exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600"); // 预检请求的有效期（秒）

            exchange.sendResponseHeaders(200, contentLength);
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) {
                return "text/html";
            } else if (path.endsWith(".css")) {
                return "text/css";
            } else if (path.endsWith(".js")) {
                return "application/javascript";
            } else if (path.endsWith(".png")) {
                return "image/png";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (path.endsWith(".gif")) {
                return "image/gif";
            } else {
                return "application/octet-stream";
            }
        }
    }
}