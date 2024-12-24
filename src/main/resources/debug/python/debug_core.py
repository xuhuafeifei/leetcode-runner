import json
import linecache
import pprint
import threading

from inst_source import inst_source
from execute_result import ExecuteResult
from log_out_helper import LogOutHelper


class Debugger:
    def __init__(self):
        self.breakpoint_list = set()
        self.pp = pprint.PrettyPrinter(indent=4)

    def trace_calls(self, frame, event, arg):
        if event == 'call':
            function_name = frame.f_code.co_name
            args = frame.f_locals
            line = frame.f_lineno
            print(f'Calling function: {function_name}')
            print(f'line: {line}')
            self.breakpoint_list.add(line)
            print("init breapoint_list with line = ", line)
        if event == 'line':
            LogOutHelper.log_out("line event....")
            line = frame.f_lineno
            # 遇到断点, 阻塞打断程序
            if line in self.breakpoint_list:
                LogOutHelper.log_out("Hit breakpoint at line = " + str(line))
                while True:
                    # 读取指令
                    # read = input("请输入指令：")
                    # instruction = json.loads(self.convert_json(read))
                    # command = inst_source.consume_input()
                    # web服务器接受请求数据: print("read command = ", command)
                    # instruction = json.loads(command)
                    LogOutHelper.log_out("准备读取指令")
                    instruction = inst_source.consume_input()
                    LogOutHelper.log_out("debug_core接受指令: " + str(instruction))

                    # 读取instruction的operation
                    operation = instruction["operation"]
                    if operation == 'P':
                        self.do_p(instruction, frame)
                    elif operation == 'N':
                        return self.do_n(instruction, frame)
                    elif operation == 'B':
                        self.do_b(instruction, frame)
                    elif operation == 'R':
                        return self.do_r(frame)
                    elif operation == 'W':
                        self.do_w(instruction, frame)
                    elif operation == 'RB':
                        self.do_rb(instruction, frame)
                    elif operation == 'SHOWB':
                        self.do_showb(instruction, frame)
                    elif operation == "RBA":
                        self.do_rba(instruction, frame)
                    else:
                        LogOutHelper.log_out("指令错误!")
        elif event == 'return':
            LogOutHelper.log_out("return event...")
            function_name = frame.f_code.co_name
            return_value = arg
            print(f'Returning from function: {function_name}')
            print('Return value:')
            self.pp.pprint(return_value)
            return None
        return self.trace_calls


    def do_p(self, inst, frame):
        # 打印局部变量
        res = "Local Variables:\n"
        # 遍历frame.f_locals, 同时将他存储到字符串中
        for key, value in frame.f_locals.items():
            v = f'{key}: {value}'
            res += v + '\n'
        r = ExecuteResult.success("P", res)
        ExecuteResult.fill_with_frame(r, frame)

        LogOutHelper.log_out(res, "doP----------")

        inst_source.store_output(r)

    def do_n(self, inst, frame):
        LogOutHelper.log_out(None, "doN-------")

        inst_source.store_output(ExecuteResult.success_no_result("N"))
        return self.trace_calls

    def do_b(self, inst, frame):
        line = int(inst['param'])
        res = "Breakpoint added at line " + str(line) + "\n"
        self.breakpoint_list.add(line)

        LogOutHelper.log_out(res, "doB--------")

        inst_source.store_output(ExecuteResult.success("B", res))

    def do_r(self, frame):
        LogOutHelper.log_out("r", "doR----------")

        inst_source.store_output(ExecuteResult.success_no_result("R"))
        return self.trace_calls

    def do_w(self, inst, frame):
        line_number = frame.f_lineno
        code_name = frame.f_code.co_name
        file_name = frame.f_code.co_filename
        source_code = linecache.getline(file_name, line_number).strip()
        res = code_name + " at line " + str(line_number) + ": " + source_code

        r = ExecuteResult.success("W", res)
        ExecuteResult.fill_with_frame(r, frame)

        LogOutHelper.log_out(res, "doW----------")

        inst_source.store_output(r)

    def do_rb(self, inst, frame):
        line = int(inst['param'])
        res = ""
        if line not in self.breakpoint_list:
            res = "breakpoint not exist!"
            LogOutHelper.log_out(res, "doRB----------")
            return
        self.breakpoint_list.remove(line)
        res = "breakpoint at line " + str(line) +  " is removed!"

        LogOutHelper.log_out(res, "doRB----------")

        inst_source.store_output(ExecuteResult.success("RB", res))

    def do_showb(self, inst, frame):
        res = ""
        for breakpoint in self.breakpoint_list:
            res += "breakpoint at line " + str(breakpoint) + "\n"

        LogOutHelper.log_out(res, "doSHOWB----------")

        inst_source.store_output(ExecuteResult.success("SHOWB", res))

    def do_rba(self, instruction, frame):
        self.breakpoint_list.clear()
        res = "All breakpoint removed!"

        LogOutHelper.log_out(res, "doRBA----------")

        inst_source.store_output(ExecuteResult.success("RBA", res))

    # 废弃
    def convert_json(self, input):
        # 将input按照空格分割,
        params = input.split(" ")
        # 如果有第二个元素, 则param不为null
        if len(params) > 1:
            # 把{}转换为json
            return json.dumps({"operation": params[0], "param": params[1]})
        return json.dumps({"operation": input, "param": None})

# 创建一个Debugger实例并使用trace_calls方法
debugger = Debugger()

# 使用trace_calls方法作为trace的钩子
def trace_calls(frame, event, arg):
    return debugger.trace_calls(frame, event, arg)
