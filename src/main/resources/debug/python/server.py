import json

from aiohttp import web

from inst_source import inst_source
from log_out_helper import LogOutHelper


class WebServer:
    def __init__(self, host='localhost', port=5005):
        self.host = host
        self.port = port
        self.app = web.Application()

    def add_routes(self):
        """添加路由"""
        self.app.add_routes([web.post('/process', self.handle_post)])

    async def handle_post(self, request):
        """处理 POST 请求"""
        # 获取请求体内容
        post_data = await request.json()

        try:
            # command = post_data.get("command")
            LogOutHelper.log_out("web服务器接受请求数据: " + str(post_data))
            inst_source.store_input(post_data)

            # 等待输出队列中有数据
            output = inst_source.consume_output()

            response = {"status": "success", "data": output.to_json(), "message": None}

        except json.JSONDecodeError:
            response = {"status": "error", "data": None, "message": "Invalid JSON data"}

        # 返回响应内容
        return web.json_response(response)

    def run(self):
        """启动 Web 服务器"""
        self.add_routes()
        web.run_app(self.app, host=self.host, port=self.port)