import json
import linecache
import pprint
import traceback

from execute_result import ExecuteResult
from inst_source import inst_source
from log_out_helper import LogOutHelper


class Debugger:
    def __init__(self, core_method_name):
        self.breakpoint_list = set()
        self.pp = pprint.PrettyPrinter(indent=4)
        self.core_method_name = core_method_name
        self.call_stack = []
        self.pre_option = None
        self.pre_param = None
        '''
        快照栈的长度, 服务于step over and step out指令
        需要说明的是, 该变量记录的是指令执行时的调用栈的状态
        '''
        self.capture_stack_len = -1
        self.cur_breakpoint = -1
        self.pre_breakpoint = -1
        self.pre_line = -1

    def trace_calls(self, frame, event, arg):
        try:
            # DEBUG
            # debug_info = "-------debug info----------" + "\n"
            debug_info = "event = " + event + "\n"
            debug_info += "line = " + str(frame.f_lineno) + "\n"
            debug_info += "call_stack = " + str(self.call_stack) + "\n"
            debug_info += "pre_option = " + str(self.pre_option) + "\n"
            debug_info += "pre_param = " + str(self.pre_param) + "\n"
            debug_info += "pre_breakpoint = " + str(self.pre_breakpoint) + "\n"
            debug_info += "pre_line = " + str(self.pre_line) + "\n"
            debug_info += "catpure_stack_len = " + str(self.capture_stack_len) + "\n"
            debug_info += "cur_method = " + frame.f_code.co_name + "\n"
            LogOutHelper.log_out(debug_info, "---------------trace_calls--------------")

            # 如果执行step over, 则一直执行到当前栈的长度和快照拍摄时栈长度一致
            if event == 'line' and self.pre_option == 'STEP' and self.pre_param == "over":
                # capture_len = -1, 表示没有初始化, 不进行判断
                if self.capture_stack_len != -1 and self.capture_stack_len != len(self.call_stack):
                    return self.trace_calls
            # 如果执行step out, 则一致执行直到当前栈长度 = 拍摄栈长度 - 1
            if event == 'line' and self.pre_option == 'STEP' and self.pre_param == "out":
                # capture_len = -1, 表示没有初始化, 不进行判断
                # call_stack 为空, 则不进行判断
                LogOutHelper.log_out("检测到上一轮执行step out指令, call_stack = " + str(self.call_stack))
                # 当前调用栈的长度不等于拍摄栈长度 - 1, 则需要持续运行, 也就是当前函数栈pop顶层方法, 也就是快照拍摄时的栈长度 - 1
                if self.capture_stack_len != -1 and len(self.call_stack) != 0 and len(self.call_stack) != self.capture_stack_len - 1:
                    return self.trace_calls
            if event == 'call':
                function_name = frame.f_code.co_name
                line = frame.f_lineno
                # res = "init breakpoint_list with line = " + str(line) + " and line + 1 = " + str(line + 1)
                # 方法入栈
                self.call_stack.append(frame.f_code.co_name)
                SELF = frame.f_locals.get('self', None)
                class_name = ""
                if (SELF == None):
                    class_name = "None"
                else:
                    class_name = SELF.__class__.__name__
                LogOutHelper.log_out("class_name = " + class_name)

                # 如果当前函数不是核心函数, 则不记录断点
                LogOutHelper.log_out("检测当前函数是不是Solution的核心函数 = " + function_name + " core_method = " + self.core_method_name)
                if function_name != self.core_method_name:
                    return self.trace_calls

                # 如果当前函数是原地调用, 也就是函数调用和函数执行是同一行, 则不记录断点
                LogOutHelper.log_out("检测当前函数是不是原地调用 = " + str(line) + " pre_breakpoint = " + str(self.pre_breakpoint))
                if line == self.pre_breakpoint:
                    return self.trace_calls
                # self.breakpoint_list.add(line)
                res = "init breakpoint_list with line + 1 = " + str(line + 1)
                LogOutHelper.log_out(res)
                self.breakpoint_list.add(line + 1)
            elif event == 'line':
                line = frame.f_lineno
                # 如果上一个指令是R, 则继续执行, 直到遇到断点
                if self.pre_option == 'R' and line not in self.breakpoint_list:
                    '''
                    这里需要跟新pre_line为line. 因为此处python执行过line行代码, 再下一轮处理过程中line将会变为pre_line
                    因此这里需要进行记录
                    '''
                    self.pre_line = line
                    return self.trace_calls
                '''
                执行R指令, 遇到断点, 则进一步检查(因为存在列表推导式这个byd, 可能会出现一直在同一行循环, 此时不做检查的话, R指令会一直卡在同一行)
                这就需要通过pre_line + line 进行判断
                pre_line与cur_line用于判断是否是单行循环, 如果相等, 表明单行循环
                '''
                # 当前断点和上一次操作的断点相同
                LogOutHelper.log_out("开始检测是否处于循环, 当前line = " + str(line) + " pre_line = " + str(self.pre_line))
                if self.pre_option == 'R' and line == self.pre_line:
                    # 判断是否是dfs递归
                    # LogOutHelper.log_out("检测到上轮执行R, 现在检测是否是dfs递归\n" + "当前call_stack = " + str(self.call_stack) + "\n" + "capture_stack_len = " + str(self.capture_stack_len))
                    # 如果相等, 则标明是循环而不是递归
                    # if len(self.call_stack) == self.capture_stack_len:
                        # 循环, 继续判断是单行还是多行. 如果当前执行行和上一次操作行相等, 则说明是单行循环, 直接滚蛋
                        if line == self.pre_line:
                            # 单行循环, 滚蛋
                            return self.trace_calls
                        else:
                            # 其余情况(目前想到的是dfs, 多行循环), 需要阻塞
                            pass
                    # else:
                        # 递归, 需要阻塞执行
                        # pass


                # 遇到断点, 阻塞打断程序
                LogOutHelper.log_out("Hit breakpoint at line = " + str(line))
                # 遇到断点, 记录栈
                self.capture_stack()
                # 遇到断点, 记录line为pre_line. 当while循环退出后, line则会成为pre_line. 因此此处做出记录
                self.pre_line = line
                self.pre_breakpoint = line

                while True:
                    # 读取指令
                    LogOutHelper.log_out("准备读取指令")
                    instruction = inst_source.consume_input()
                    LogOutHelper.log_out("debug_core接受指令: " + str(instruction))

                    # 读取instruction的operation
                    operation = instruction["operation"]
                    param = instruction["param"]
                    # 记录当前操作为上一轮运行操作(r, n, step out/over)
                    if operation == 'R' or operation == 'N' or operation == 'STEP':
                        self.pre_option = operation
                        self.pre_param = param
                    self.pre_breakpoint = line  # 当前遇到的断点将会是下一轮操作的pre_breakpoint

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
                    elif operation == "STEP":
                        return self.do_step(instruction, frame)
                    else:
                        LogOutHelper.log_out("指令错误!")
                        inst_source.store_output(ExecuteResult.success_no_result("指令错误"))
            elif event == 'return':
                LogOutHelper.log_out("return event...")
                function_name = frame.f_code.co_name
                return_value = arg
                self.call_stack.pop()
                print(f'Returning from function: {function_name}')
                print('Return value:')
                self.pp.pprint(return_value)
                return None
            return self.trace_calls
        except Exception as e:
            traceback.print_exc()
            inst_source.store_output(ExecuteResult.fail("指令执行出错" + str(e)))

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
        file_name = frame.f_code.co_filename
        source_code = linecache.getline(file_name, line_number).strip()
        res = source_code

        r = ExecuteResult.success("W", res)
        ExecuteResult.fill_with_frame(r, frame)

        LogOutHelper.log_out(res, "doW----------")

        inst_source.store_output(r)

    def do_rb(self, inst, frame):
        line = int(inst['param'])
        LogOutHelper.log_out("breakpoint_list = " + str(self.breakpoint_list))
        res = ""
        if line not in self.breakpoint_list:
            res = "breakpoint not exist!"
            LogOutHelper.log_out(res, "doRB----------")
            inst_source.store_output(ExecuteResult.success("RB", res))
            return
        self.breakpoint_list.remove(line)
        res = "breakpoint at line " + str(line) + " is removed!"

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

    def do_step(self, instruction, frame):
        # 记录栈
        # self.capture_stack()
        inst_source.store_output(ExecuteResult.success_no_result("STEP"))
        return self.trace_calls

    def capture_stack(self):
        self.capture_stack_len = len(self.call_stack)

# # 创建一个Debugger实例并使用trace_calls方法
# debugger = Debugger()
#
# # 使用trace_calls方法作为trace的钩子
# def trace_calls(frame, event, arg):
#     return debugger.trace_calls(frame, event, arg)
