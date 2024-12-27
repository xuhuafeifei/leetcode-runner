import json
import traceback
from aiohttp import web

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
    def __init__(self, log, inst_source, host='localhost', port=5005):
        self.host = host
        self.port = port
        self.app = web.Application()
        self.log = log
        self.inst_source = inst_source

    def add_routes(self):
        """添加路由"""
        self.app.add_routes([web.post('/process', self.handle_post)])

    async def handle_post(self, request):
        """处理 POST 请求"""

        try:
            # 获取请求体内容
            self.log.log_out("web准备接受数据")
            post_data = await request.json()

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

        # 返回响应内容，将 Response 转换为字典
        return web.json_response(response.to_dict())

    def run(self):
        """启动 Web 服务器"""
        self.add_routes()
        web.run_app(self.app, host=self.host, port=self.port)
