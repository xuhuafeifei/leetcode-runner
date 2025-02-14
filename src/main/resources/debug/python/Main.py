import sys
import threading
import os

from typing import List, Optional, Tuple, Dict, Set
from collections import defaultdict, Counter, deque, namedtuple, OrderedDict
from heapq import heappush, heappop, heapify
from functools import lru_cache, cache
from itertools import permutations, combinations, product, accumulate
from bisect import bisect_left, bisect_right
from ListNode import ListNode
from TreeNode import TreeNode
import math

from inst_source import InstSource
from log_out_helper import LogOutHelper
from debug_core import Debugger

from Solution import Solution
from server import WebServer

log_dir = "D:\\java_code\\lc\\src\\cache\\debug\\python\\pylog\\run_log.txt"
std_out_dir = "D:\\java_code\\lc\\src\\cache\\debug\\python\\pylog\\py_std_out.log"
std_err_dir = "D:\\java_code\\lc\\src\\cache\\debug\\python\\pylog\\py_std_err.log"

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

    # sys.stdout = MyOutput(std_out_dir)
    # sys.stderr = MyOutput(std_err_dir)

    log_out_helper = LogOutHelper(log_dir)
    inst_source = InstSource(log_out_helper)
    # 启动服务, 开启新的线程
    # 创建线程
    webServer = WebServer(log=log_out_helper, inst_source=inst_source)
    thread = threading.Thread(target=webServer.run, daemon=True)
    # 启动线程
    thread.start()

    solution = Solution()
    trace_calls = Debugger('merge', 'std_in', log_out_helper, inst_source).trace_calls
    sys.settrace(trace_calls)
    a0 = [1,2,3,0,0,0]
    a1 = 3
    a2 = [4,5,6]
    a3 = 3
    result = solution.merge(a0,a1,a2,a3)

    sys.settrace(None)  # 停止跟踪

if __name__ == '__main__':
    main()