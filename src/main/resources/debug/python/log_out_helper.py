class LogOutHelper:
    def __init__(self):
        pass

    @classmethod
    def log_out(cls, message: str, title: str = None):
        if title is not None:
            print(title)
        if message is not None:
            print(message)
        print()