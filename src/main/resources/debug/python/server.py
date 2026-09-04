import json
import threading
import time
import traceback
from http.server import HTTPServer, SimpleHTTPRequestHandler


class Response:
    def __init__(self, status, data, message):
        self.status = status
        self.data = data  # data 是 ExecuteResult 类型
        self.message = message

    def to_dict(self):
        """将 Response 对象转换为字典"""
        return {
            'status': self.status,
            'data': self.data.to_dict() if self.data else None,
            'message': self.message
        }


class WebServer:
    """
    调试指令入口 HTTP 服务.

    空闲超时: 超过 idle_timeout 秒没有收到任何指令则主动关闭服务并退出进程,
    避免 IDEA 崩溃/强杀后遗留僵尸 python 进程 (曾导致后台狂写 22GB 日志).
    """

    def __init__(self, log, inst_source, host='localhost', port=5015, idle_timeout=1800):
        self.host = host
        self.port = port
        self.log = log
        self.inst_source = inst_source
        self.idle_timeout = idle_timeout
        self.last_active_ts = time.time()
        self.server = None

    def _touch(self):
        """记录最近一次活跃时间"""
        self.last_active_ts = time.time()

    def run(self):
        """启动 Web 服务器 + 空闲看门狗"""
        handler = self.create_handler(self.log, self.inst_source)
        self.server = HTTPServer((self.host, self.port), handler)
        self.log.log_out(f"Starting server on {self.host}:{self.port}")

        # 看门狗线程: 空闲超过阈值则关闭服务, 进程随之结束
        def watchdog():
            while True:
                time.sleep(30)
                idle = time.time() - self.last_active_ts
                if idle > self.idle_timeout:
                    self.log.log_out(f"Server idle for {int(idle)}s, shutting down.")
                    try:
                        self.server.shutdown()
                    except Exception:
                        pass
                    # 兜底: 无论进程卡在哪, 直接退出
                    import os
                    os._exit(0)

        threading.Thread(target=watchdog, daemon=True).start()

        try:
            self.server.serve_forever()
        except Exception:
            pass
        # serve_forever 退出后进程也必须退出, 防止僵尸
        self.log.log_out("Web server stopped, exiting process.")
        import os
        os._exit(0)

    def create_handler(self, _log, _inst_source):
        """创建请求处理类"""
        web = self

        class RequestHandler(SimpleHTTPRequestHandler):
            def __init__(self, *args, **kwargs):
                self.log = _log
                self.inst_source = _inst_source
                super().__init__(*args, **kwargs)

            def log_message(self, format, *args):
                # HTTP 访问日志默认关闭, 避免日志膨胀
                pass

            def do_POST(self):
                web._touch()
                try:
                    content_length = int(self.headers['Content-Length'])
                    post_data = self.rfile.read(content_length)
                    post_data = json.loads(post_data.decode('utf-8'))

                    _log.log_out("web服务器接受请求数据: " + str(post_data))
                    self.inst_source.store_input(post_data)

                    # 等待输出队列中有数据
                    output = self.inst_source.consume_output()

                    if output is None:
                        # 调试线程 60s 未回结果, 明确返回错误而不是崩在 to_dict
                        response = Response(status="error", data=None, message="debugger timeout: no response in 60s")
                    else:
                        _log.log_out(str(output))
                        response = Response(status="success", data=output, message=None)

                except json.JSONDecodeError:
                    traceback.print_exc()
                    response = Response(status="error", data=None, message="Invalid JSON data")
                except Exception as e:
                    traceback.print_exc()
                    response = Response(status="error", data=None, message=str(e))

                # 返回响应内容
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps(response.to_dict()).encode('utf-8'))

        return RequestHandler
