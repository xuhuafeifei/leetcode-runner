"""
对应Java的ExecuteResult
"""

class ExecuteResult:
    def __init__(self, operation=None, success=False, has_result=False, result=None, msg=None, add_line=0,
                 method_name=None, context=None, class_name=None, more_info=None):
        self.operation = operation
        self.success = success
        self.has_result = has_result
        self.result = result
        self.msg = msg
        self.add_line = add_line
        self.method_name = method_name
        self.context = context
        self.class_name = class_name
        self.more_info = more_info

    @classmethod
    def fill_with_frame(cls, r: 'ExecuteResult', frame):
        """根据帧信息填充执行结果"""
        r.class_name = frame.f_locals.get('self', None).__class__.__name__
        r.add_line = frame.f_lineno
        r.method_name = frame.f_code.co_name

    @classmethod
    def success(cls, operation, result=None):
        """成功的执行结果"""
        return cls(operation=operation, success=True, has_result=True, result=result)

    @classmethod
    def success_no_result(cls, operation):
        """成功但没有结果的执行"""
        return cls(operation=operation, success=True, has_result=False)

    @classmethod
    def fail(cls, operation, msg="Some error happens!"):
        """失败的执行结果"""
        return cls(operation=operation, success=False, msg=msg)

    def __str__(self):
        return f"ExecuteResult(operation={self.operation}, success={self.success}, " \
               f"has_result={self.has_result}, result={self.result}, msg={self.msg}, " \
               f"add_line={self.add_line}, method_name={self.method_name}, context={self.context}, " \
               f"class_name={self.class_name}, more_info={self.more_info})"
    def to_dict(self):
        """将对象转换为字典，便于序列化"""
        return {
            'operation': self.operation,
            'success': self.success,
            'has_result': self.has_result,
            'result': self.result,
            'msg': self.msg,
            'add_line': self.add_line,
            'method_name': self.method_name,
            'context': self.context,
            'class_name': self.class_name,
            'more_info': self.more_info
        }

    def to_json(self):
        """将对象转换为 JSON 字符串"""
        import json
        return json.dumps(self.to_dict())

