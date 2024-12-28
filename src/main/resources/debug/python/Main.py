import sys
import threading
import os

from inst_source import InstSource
from log_out_helper import LogOutHelper
from debug_core import Debugger

from Solution import Solution
from server import WebServer

log_dir = "E:\\java_code\\lc-test\\cache\\debug\\run_log.txt"
std_out_dir = "E:\\java_code\\lc-test\\cache\\debug\\py_std_out.log"
std_err_dir = "E:\\java_code\\lc-test\\cache\\debug\\py_std_err.log"

class MyOutput():
    def __init__(self, file_path):
        self.file_path = file_path

    def write(self, data):
        with open(self.file_path, 'a', encoding='utf-8') as f:
            f.write(data)

    def flush(self):
        pass

def main():
    # 确保目录存在
    os.makedirs(os.path.dirname(log_dir), exist_ok=True)
    os.makedirs(os.path.dirname(std_out_dir), exist_ok=True)
    os.makedirs(os.path.dirname(std_err_dir), exist_ok=True)

    # 创建全新的文件
    with open(log_dir, 'w') as f:
        pass
    with open(std_out_dir, 'w') as f:
        pass
    with open(std_err_dir, 'w') as f:
        pass

    sys.stdout = MyOutput(std_out_dir)
    sys.stderr = MyOutput(std_err_dir)

    log_out_helper = LogOutHelper(log_dir)
    inst_source = InstSource(log_out_helper)
    # 启动服务, 开启新的线程
    # 创建线程
    webServer = WebServer(log=log_out_helper, inst_source=inst_source)
    thread = threading.Thread(target=webServer.run, daemon=True)
    # 启动线程
    thread.start()

    solution = Solution()
    trace_calls = Debugger('totalNQueens', 'std_in', log_out_helper, inst_source).trace_calls
    sys.settrace(trace_calls)
    result = solution.totalNQueens(2)
    sys.settrace(None)  # 停止跟踪


if __name__ == '__main__':
    main()