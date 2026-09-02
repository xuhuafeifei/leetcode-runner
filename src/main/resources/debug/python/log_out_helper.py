import logging


class LogOutHelper:
    def __init__(self, log_dir="run_log.log", enabled=False):
        """
        :param enabled: 日志总开关. 默认关闭.
        debug_core 的 trace 事件量级是语句级的, 全量落盘会导致日志文件
        以 GB 速度膨胀, 因此仅在排查调试器自身问题时临时开启.
        """
        self.enabled = enabled
        self.log_dir = log_dir
        if not enabled:
            return
        logging.basicConfig(
            filename=self.log_dir,
            level=logging.DEBUG,
            format='%(asctime)s - %(message)s',
            filemode='a',
            encoding='utf-8'
        )

    def log_out(self, message: str, title: str = None):
        if not self.enabled:
            return
        log_entry = ""
        if title:
            log_entry += f"Title: {title}\n"
        if message:
            log_entry += f"Message: {message}\n"
        logging.debug(log_entry)
