import sys
import threading

from debug_core import Debugger

from Solution import Solution
from server import WebServer


def main():
    # 启动服务, 开启新的线程
    # 创建线程
    webServer = WebServer()
    thread = threading.Thread(target=webServer.run, daemon=True)
    # 启动线程
    thread.start()

    solution = Solution()
    trace_calls = Debugger('totalNQueens').trace_calls
    sys.settrace(trace_calls)
    result = solution.totalNQueens(4)
    sys.settrace(None)  # 停止跟踪


if __name__ == '__main__':
    main()