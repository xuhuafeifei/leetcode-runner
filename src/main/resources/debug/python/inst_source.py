import queue

from log_out_helper import LogOutHelper


class InstSource:
    def __init__(self, log, wait_timeout=60):
        """
        :param wait_timeout: 阻塞等待指令/消费结果的超时秒数.
        超时返回 None, 避免 Java 端异常退出导致 python 端永久挂起.
        """
        self.log = log
        self.wait_timeout = wait_timeout
        self._input_queue = queue.Queue()
        self._output_queue = queue.Queue()

    def store_input(self, item):
        self.log.log_out(f"Storing input item: {item}")
        self._input_queue.put(item)

    def consume_input(self):
        try:
            item = self._input_queue.get(timeout=self.wait_timeout)
            self.log.log_out(f"Consumed input item: {item}")
            return item
        except queue.Empty:
            self.log.log_out("Input queue is empty (timeout).")
            return None

    def store_output(self, item):
        self.log.log_out(f"Storing output item: {item}")
        self._output_queue.put(item)

    def consume_output(self):
        try:
            item = self._output_queue.get(timeout=self.wait_timeout)
            self.log.log_out(f"Consumed output item: {item}")
            return item
        except queue.Empty:
            self.log.log_out("Output queue is empty (timeout).")
            return None
