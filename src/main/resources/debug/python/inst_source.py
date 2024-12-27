import queue

from log_out_helper import LogOutHelper


class InstSource:
    def __init__(self, log):
        # 定义两个类变量，一个输入阻塞队列，一个输出阻塞队列
        self.log = log
        self._input_queue = queue.Queue()
        self._output_queue = queue.Queue()

    def store_input(self, item):
        """将数据存入输入队列"""
        self.log.log_out(f"Storing input item: {item}")
        self._input_queue.put(item)

    def consume_input(self):
        """从输入队列消费数据"""
        try:
            item = self._input_queue.get()  # 阻塞等待5秒
            self.log.log_out(f"Consumed input item: {item}")
            return item
        except queue.Empty:  # 捕获queue.Empty异常
            LogOutHelper.log_out("Input queue is empty.")
            return None

    def store_output(self, item):
        """将数据存入输出队列"""
        self.log.log_out(f"Storing output item: {item}")
        self._output_queue.put(item)

    def consume_output(self):
        """从输出队列消费数据"""
        try:
            item = self._output_queue.get()  # 阻塞等待5秒
            self.log.log_out(f"Consumed output item: {item}")
            return item
        except queue.Empty:  # 捕获queue.Empty异常
            self.log.log_out("Output queue is empty.")
            return None