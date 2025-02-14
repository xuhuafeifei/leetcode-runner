import logging

class LogOutHelper:
    def __init__(self, log_dir="run_log.log"):
        # 配置日志记录器，使用日志文件
        self.log_dir = log_dir

        # 配置logging，设置日志级别为DEBUG
        logging.basicConfig(
            filename=self.log_dir,
            level=logging.DEBUG,  # 只需要DEBUG级别
            format='%(asctime)s - %(message)s',  # 简单的时间戳和消息
            filemode='a',  # 以追加模式打开文件
            encoding='utf-8'  # 指定编码为 UTF-8
        )

    def log_out(self, message: str, title: str = None):
        '''
        Write debug log entries to the log file.

        :param message: Log message to be written to file
        :param title: Optional title for the log entry
        '''
        log_entry = ""
        if title:
            log_entry += f"Title: {title}\n"
        if message:
            log_entry += f"Message: {message}\n"

        # 记录日志
        logging.debug(log_entry)
        # print(log_entry)
