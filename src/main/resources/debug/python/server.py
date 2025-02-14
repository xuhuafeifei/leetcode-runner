import json
import traceback
from http.server import HTTPServer, SimpleHTTPRequestHandler


class Response:
    def __init__(self, status, data, message):
        self.status = status
        self.data = data  # `data` 是 ExecuteResult 类型
        self.message = message

    def to_dict(self):
        """将 Response 对象转换为字典"""
        return {
            'status': self.status,
            'data': self.data.to_dict() if self.data else None,  # 确保 `data` 是字典
            'message': self.message
        }

class WebServer:
    def __init__(self, log, inst_source, host='localhost', port=5015):
        self.host = host
        self.port = port
        self.log = log
        self.inst_source = inst_source

    def run(self):
        """启动 Web 服务器"""
        handler = self.create_handler(self.log, self.inst_source)
        # handler = SimpleHTTPRequestHandler2
        server = HTTPServer((self.host, self.port), handler)
        self.log.log_out(f"Starting server on {self.host}:{self.port}")
        server.serve_forever()

    def create_handler(self, _log, _inst_source):
        """创建请求处理类"""
        class RequestHandler(SimpleHTTPRequestHandler):
            def __init__(self, *args, **kwargs):
                self.log = _log
                self.inst_source = _inst_source
                # 传递 WebServer 的 log 到 RequestHandler
                super().__init__(*args, **kwargs)

            def log_message(self, format, *args):
                self.log.log_out(format % args)

            def do_POST(self):
                self.log.log_out("起订!")
                """处理 POST 请求"""
                try:
                    content_length = int(self.headers['Content-Length'])
                    post_data = self.rfile.read(content_length)
                    post_data = json.loads(post_data.decode('utf-8'))

                    self.log.log_out("web服务器接受请求数据: " + str(post_data))
                    self.inst_source.store_input(post_data)

                    # 等待输出队列中有数据
                    output = self.inst_source.consume_output()

                    self.log.log_out(output)

                    # 创建 Response 对象
                    response = Response(status="success", data=output, message=None)

                except json.JSONDecodeError:
                    traceback.print_exc()
                    # 创建错误的 Response 对象
                    response = Response(status="error", data=None, message="Invalid JSON data")
                except Exception as e:
                    traceback.print_exc()
                    # 创建异常的 Response 对象
                    response = Response(status="error", data=None, message=str(e))

                # 返回响应内容
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps(response.to_dict()).encode('utf-8'))

        return RequestHandler
