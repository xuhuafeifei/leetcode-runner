import queue
import threading
import time

from log_out_helper import LogOutHelper


class inst_source:
    # 定义两个类变量，一个输入阻塞队列，一个输出阻塞队列
    _input_queue = queue.Queue()
    _output_queue = queue.Queue()

    @classmethod
    def store_input(cls, item):
        """将数据存入输入队列"""
        LogOutHelper.log_out(f"Storing input item: {item}")
        cls._input_queue.put(item)

    @classmethod
    def consume_input(cls):
        """从输入队列消费数据"""
        try:
            item = cls._input_queue.get()  # 阻塞等待5秒
            LogOutHelper.log_out(f"Consumed input item: {item}")
            return item
        except queue.Empty:  # 捕获queue.Empty异常
            LogOutHelper.log_out("Input queue is empty.")
            return None

    @classmethod
    def store_output(cls, item):
        """将数据存入输出队列"""
        LogOutHelper.log_out(f"Storing output item: {item}")
        cls._output_queue.put(item)

    @classmethod
    def consume_output(cls):
        """从输出队列消费数据"""
        try:
            item = cls._output_queue.get()  # 阻塞等待5秒
            LogOutHelper.log_out(f"Consumed output item: {item}")
            return item
        except queue.Empty:  # 捕获queue.Empty异常
            LogOutHelper.log_out("Output queue is empty.")
            return None


'''
测试demo
'''
def consume():
    while True:
        out = inst_source.consume_output()
        if out is not None:
            LogOutHelper.log_out(f"Consumed from output: {out}")
        time.sleep(1)  # Sleep to simulate processing time

def produce():
    while True:
        inst_source.store_output("hello")
        time.sleep(3)  # Sleep for 3 seconds before producing next item


# 启动事件循环
if __name__ == "__main__":
    t1 = threading.Thread(target=produce)
    t1.start()

    t2 = threading.Thread(target=consume)
    t2.start()
