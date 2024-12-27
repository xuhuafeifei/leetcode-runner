import json
import linecache
import traceback

from execute_result import ExecuteResult


class Debugger:
    def __init__(self, core_method_name, read_type, log_out_helper, inst_source):
        self.inst_source = inst_source
        self.log = log_out_helper
        self.breakpoint_list = set()
        self.core_method_name = core_method_name
        self.call_stack = []
        # pre_option记录的是上一轮运行操作(R, N, STEP...)
        self.pre_option = 'R'
        self.pre_param = None
        '''
        这里的read_type和ReadType.java一致. 有关readType详细信息
        可以参考com.xhf.leetcode.plugin.debug.reader.ReadType
        '''
        self.read_type = read_type
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
            self.log.log_out(debug_info, "---------------trace_calls--------------")

            # 如果执行step over, 则一直执行到当前栈的长度和快照拍摄时栈长度一致
            if event == 'line' and self.pre_option == 'STEP' and self.pre_param == "over":
                # capture_len = -1, 表示没有初始化, 不进行判断
                if self.capture_stack_len != -1 and self.capture_stack_len != len(self.call_stack):
                    return self.trace_calls
            # 如果执行step out, 则一致执行直到当前栈长度 = 拍摄栈长度 - 1
            if event == 'line' and self.pre_option == 'STEP' and self.pre_param == "out":
                # capture_len = -1, 表示没有初始化, 不进行判断
                # call_stack 为空, 则不进行判断
                self.log.log_out("检测到上一轮执行step out指令, call_stack = " + str(self.call_stack))
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
                if SELF is None:
                    class_name = "None"
                else:
                    class_name = SELF.__class__.__name__
                self.log.log_out("class_name = " + class_name)

                # 如果当前函数不是核心函数, 则不记录断点
                self.log.log_out("检测当前函数是不是Solution的核心函数 = " + function_name + " core_method = " + self.core_method_name)
                if function_name != self.core_method_name:
                    return self.trace_calls

                '''
                更新: 这段检查其实没必要, 因为event = call的断点逻辑是: 只会在core_method方法打上断点
                
                如果当前函数是原地调用, 也就是函数调用和函数执行是同一行, 则不记录断点
                比如某个出生列表推导式, 他就是原地调用. 对于这种调用类型, 不能在调用函数line的下一行
                打断点. 否则对使用者来说会莫名其妙多了个断点
                '''
                self.log.log_out("检测当前函数是不是原地调用 = " + str(line) + " pre_breakpoint = " + str(self.pre_breakpoint))
                if line == self.pre_breakpoint:
                    return self.trace_calls

                self.log.log_out("检测PythonDebugger的ReadType: " + self.read_type)
                if self.read_type != "ui_in":
                    res = "检测到plugin非UI指令读取, 自动初始化断点. init breakpoint_list with line + 1 = " + str(line + 1)
                    self.log.log_out(res)
                    self.breakpoint_list.add(line + 1)
                else:
                    self.log.log_out("检测到plugin为UI指令读取, 等待初始化断点....")
                    while True:
                        # 读取指令
                        self.log.log_out("准备init断点信息....")
                        instruction = self.inst_source.consume_input()
                        self.log.log_out("接受断点信息: " + str(instruction))
                        # 读取instruction的operation
                        operation = instruction["operation"]
                        '''
                        这里和PythonDebugger协商好, 当执行到 PYTHON_INIT_BREAK_DONE指令, 表示
                        断点初始化结束
                        另外, 执行断点初始化操作时, 只允许PythonDebugger发送B指令(断点指令)
                        '''
                        if operation == "PYTHON_INIT_BREAKPOINT_DONE":
                            self.inst_source.store_output(ExecuteResult.success(operation, "结束断点init"))
                            break
                        if operation != "B":
                            self.log.log_out("检测到PythonDebugger发送的指令不是B指令, 不进行断点初始化, 退出....")
                            self.inst_source.store_output(ExecuteResult.success(operation, "检测到plugin发送的指令不是B指令, 不进行断点初始化, "
                                                                                      "退出...."))
                            return self.trace_calls
                        self.do_b(instruction, frame)

            elif event == 'line':
                function_name = frame.f_code.co_name
                # 获取当前类名字
                SELF = frame.f_locals.get('self', None)
                class_name = ""
                if SELF is not None:
                    class_name = SELF.__class__.__name__

                self.log.log_out("检测当前类名字 = " + class_name + " 当前方法名字 = " + function_name)
                if class_name in ['MyOutput', 'IncrementalDecoder']:
                    return self.trace_calls
                # skip, 这些函数都是MyOutput重定向stdout后导致的系统函数, 不需要在这些地方阻塞
                if function_name in ['write', 'getpreferredencoding']:
                    return self.trace_calls

                line = frame.f_lineno
                '''
                如果是R操作, 执行代码, 直到遇到断点. 如果是STEP, N, 则不需要运行到断点
                '''
                if self.pre_option == "STEP" or self.pre_option == 'N':
                    pass
                if line not in self.breakpoint_list and self.pre_option == 'R':
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
                self.log.log_out("开始检测是否处于循环, 当前line = " + str(line) + " pre_line = " + str(self.pre_line))
                if self.pre_option == 'R' and line == self.pre_line:
                    # 判断是否是dfs递归
                    # self.log.log_out("检测到上轮执行R, 现在检测是否是dfs递归\n" + "当前call_stack = " + str(self.call_stack) + "\n" + "capture_stack_len = " + str(self.capture_stack_len))
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
                if self.pre_option == 'R':
                    self.log.log_out("Hit breakpoint at line = " + str(line))
                elif self.pre_option == 'STEP' or self.pre_option == 'N':
                    self.log.log_out(self.pre_option + " at line = " + str(line))
                # 遇到断点, 记录栈
                self.capture_stack()
                # 遇到断点, 记录line为pre_line. 当while循环退出后, line则会成为pre_line. 因此此处做出记录
                self.pre_line = line
                self.pre_breakpoint = line

                while True:
                    # 读取指令
                    self.log.log_out("准备读取指令")
                    instruction = self.inst_source.consume_input()
                    self.log.log_out("debug_core接受指令: " + str(instruction))

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
                        self.log.log_out("指令错误!")
                        self.inst_source.store_output(ExecuteResult.success(operation, "指令错误"))
            elif event == 'return':
                self.log.log_out("return event...")
                function_name = frame.f_code.co_name
                return_value = arg
                self.call_stack.pop()
                res = 'Returning from function: ' + function_name + "\n"
                # res += "Return value: " + ("NULL" if return_value is None else return_value)
                res += f"Return value: {return_value}"
                self.log.log_out(res, "return-----------------")
                return None
            return self.trace_calls
        except Exception as e:
            traceback.print_exc()
            self.inst_source.store_output(ExecuteResult.fail("指令执行出错" + str(e)))

    def do_p(self, inst, frame):
        # 打印局部变量
        res = "Local Variables:\n"
        # 遍历frame.f_locals, 同时将他存储到字符串中
        for key, value in frame.f_locals.items():
            v = f'{key}: {value}'
            res += v + '\n'
        r = ExecuteResult.success("P", res)
        ExecuteResult.fill_with_frame(r, frame)

        self.log.log_out(res, "doP----------")

        self.inst_source.store_output(r)

    def do_n(self, inst, frame):
        self.log.log_out(None, "doN-------")

        self.inst_source.store_output(ExecuteResult.success_no_result("N"))
        return self.trace_calls

    def do_b(self, inst, frame):
        line = int(inst['param'])
        res = "Breakpoint added at line " + str(line) + "\n"
        self.breakpoint_list.add(line)

        self.log.log_out(res, "doB--------")

        self.inst_source.store_output(ExecuteResult.success("B", res))

    def do_r(self, frame):
        self.log.log_out("r", "doR----------")

        self.inst_source.store_output(ExecuteResult.success_no_result("R"))
        return self.trace_calls

    def do_w(self, inst, frame):
        line_number = frame.f_lineno
        file_name = frame.f_code.co_filename
        source_code = linecache.getline(file_name, line_number).strip()
        res = source_code

        r = ExecuteResult.success("W", res)
        ExecuteResult.fill_with_frame(r, frame)

        self.log.log_out(res, "doW----------")

        self.inst_source.store_output(r)

    def do_rb(self, inst, frame):
        line = int(inst['param'])
        self.log.log_out("breakpoint_list = " + str(self.breakpoint_list))
        res = ""
        if line not in self.breakpoint_list:
            res = "breakpoint not exist!"
            self.log.log_out(res, "doRB----------")
            self.inst_source.store_output(ExecuteResult.success("RB", res))
            return
        self.breakpoint_list.remove(line)
        res = "breakpoint at line " + str(line) + " is removed!"

        self.log.log_out(res, "doRB----------")

        self.inst_source.store_output(ExecuteResult.success("RB", res))

    def do_showb(self, inst, frame):
        res = ""
        for breakpoint in self.breakpoint_list:
            # 这个必须加\n, 因为要给客户端一个回车
            res += "breakpoint at line " + str(breakpoint) + "\n"

        self.log.log_out(res, "doSHOWB----------")

        self.inst_source.store_output(ExecuteResult.success("SHOWB", res))

    def do_rba(self, instruction, frame):
        self.breakpoint_list.clear()
        res = "All breakpoint removed!"

        self.log.log_out(res, "doRBA----------")

        self.inst_source.store_output(ExecuteResult.success("RBA", res))

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
        self.inst_source.store_output(ExecuteResult.success_no_result("STEP"))
        return self.trace_calls

    def capture_stack(self):
        self.capture_stack_len = len(self.call_stack)

# # 创建一个Debugger实例并使用trace_calls方法
# debugger = Debugger()
#
# # 使用trace_calls方法作为trace的钩子
# def trace_calls(frame, event, arg):
#     return debugger.trace_calls(frame, event, arg)
