//
// Created by 25080 on 2025/1/30.
//
#pragma once
#ifndef CPP_GDBCONTROLLER_H
#define CPP_GDBCONTROLLER_H

#include <windows.h>
#include <tchar.h>
#include <iostream>
#include <strsafe.h>

#include <string>
#include <thread>
#include <utility>
#include "ExecuteResult.h"
#include "GdbInstruction.h"
#include "leetcode.h"
#include "LogHelper.h"

#define BUFSIZE 128
#define BUFFER_SIZE (1024 * 2)

typedef struct CPP_GDB_INFO {
    std::string stopped_reason;
    std::string console_output;
    std::string status;
    std::string log_output;
    std::string result_record;

    std::string to_string() const {
        // return R"({"stopped_reason":")" + this->stopped_reason + R"(","console_output":")" + console_output + R"(","status":")" + status + R"(","log_output":")" + log_output + "\"}";
        json j;
        j["stopped_reason"] = this->stopped_reason;
        j["console_output"] = this->console_output;
        j["status"] = this->status;
        j["log_output"] = this->log_output;
        j["result_record"] = this->result_record;
        return j.dump();
    }
    json to_json() const {
        return json{
                {"stopped_reason", this->stopped_reason},
                {"console_output", this->console_output},
                {"status", this->status},
                {"log_output", this->log_output},
                {"result_record", this->result_record}
        };
    }
} CPP_GDB_INFO;

std::string to_string(const CPP_GDB_INFO& c) {
    return R"({"stopped_reason":")" + c.stopped_reason + R"(","console_output":")" + c.console_output + R"(","status":")" + c.status + R"(","log_output":")" + c.log_output + "\"}";
}

typedef struct READ_INFO {
    DWORD total_read;
    bool has;
} READ_INFO;

class GdbController {
public:
    GdbController(std::string gdb_path, std::string exe_path, std::string gdb_param, LogHelper& log);
    void start_gdb();
private:
    std::string gdb_path;
    std::string exe_path;
    std::string gdb_param;
protected:
    char buffer[BUFFER_SIZE]{};

    HANDLE gdb_std_in_rd = nullptr;
    HANDLE gdb_std_in_wr = nullptr;
    HANDLE gdb_std_out_rd = nullptr;
    HANDLE gdb_std_out_wr = nullptr;

    LogHelper log;
protected:
    virtual GdbInstruction get_command() = 0;
    virtual void show_gdb_output(ExecuteResult& r) = 0;

    bool is_finish(const std::string& command) const  {
        return command == "exit" || command == "quit" || command == "q";
    }
    void io_with_gdb();
    DWORD write_to_gdb(const char buf[], const DWORD write_cnt);
    /**
     * 判断ReadFile是否已经读取到gdb输出的结尾
     */
    bool read_end(char buf[], int end);
    /**
     * 读取GDB的输出, 同时判断本轮读取是否将GDB全部的输出读取完成
     *
     * 在从gdb中读取数据的过程中, 可能因为多种原因导致GDB的输出没有完全读取, 比如buf的大小小于GDB输出的内容长度
     * 又或者GBD产生多轮事件, 存在多次输出. 比如执行r指令, 会产生r运行的中间数据 + 遇到断点的数据. 每一轮的输出结尾都会以(gdb)
     * 作为标识符
     *
     * 为了应对上述情况, @auth feigebuge 封装READ_INFO结构体, 存储每次读取数据大小, 以及是否读取到gdb的结尾
     *
     * 关于判断是否结尾, 总共有两轮判断
     * 1. 检测是由以(gdb)结尾
     * 2. 让当前线程放弃cpu, 睡眠10ms, 等待gdb输出, 如果PeekNamedPipe发现存在数据, 则认为没有结束
     *
     * @param buf buf
     * @return  read_info
     */
    READ_INFO read_from_gdb(char buf[]);
    /**
     * 读取GDB的输出, 如果没有读取完全, 循环处理
     */
    std::string read_and_handle_from_gdb();
    /**
     * 创建GDB进程
     */
    void create_gdb_process();
    /**
     * 解析gdb的輸出
     */
    ExecuteResult parse_gdb_output(const std::string& gdb_result, const GdbInstruction& gdbInst);

    static string extract_text(const string &basicString);

    string extract_error(string line);
};

GdbController::GdbController(std::string gdb_path, std::string exe_path, std::string gdb_param, LogHelper& log):
        gdb_path(std::move(gdb_path)), exe_path(std::move(exe_path)), gdb_param(std::move(gdb_param)) {
    this->log = log;
};

void GdbController::start_gdb() {
    SECURITY_ATTRIBUTES saAttr;

    printf("\n->Start of parent execution.\n");

    // 设置SECURITY_ATTRIBUTES结构体的属性，使得管道句柄可以被继承
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES);
    saAttr.bInheritHandle = TRUE;  // 允许句柄继承
    saAttr.lpSecurityDescriptor = nullptr;  // 使用默认的安全描述符

    // 创建一个管道用于子进程的标准输出(STDOUT)
    if (!CreatePipe(&gdb_std_out_rd, &gdb_std_out_wr, &saAttr, 0)) {
        throw std::runtime_error("StdoutRd CreatePipe 失败");
    }

    // 确保读取端句柄不会被子进程继承
    if (!SetHandleInformation(gdb_std_out_rd, HANDLE_FLAG_INHERIT, 0)) {
        throw std::runtime_error("Stdout SetHandleInformation 失败");
    }

    // 创建一个管道用于子进程的标准输入(STDIN)
    if (!CreatePipe(&gdb_std_in_rd, &gdb_std_in_wr, &saAttr, 0)) {
        throw std::runtime_error("Stdin CreatePipe 失败");
    }

    // 确保写入端句柄不会被子进程继承
    if (!SetHandleInformation(gdb_std_in_wr, HANDLE_FLAG_INHERIT, 0)) {
        throw std::runtime_error("Stdin SetHandleInformation 失败");
    }

    // 创建子进程，这里需要传入正确的文件名参数
    create_gdb_process();

    // 读取GDB初始化的信息
    read_and_handle_from_gdb();

    // 进行与GDB的I/O操作
    io_with_gdb();

    // 关闭标准输入写入句柄，以便子进程停止读取
    if (!CloseHandle(gdb_std_in_wr)) {
        throw std::runtime_error("StdInWr CloseHandle 失败");
    }
}

std::string GdbController::read_and_handle_from_gdb() {
    auto read_info = read_from_gdb(buffer);
    std::string gdb_result(buffer, read_info.total_read);

    while (read_info.has) {
        log.log_info("start to read again!");
        read_info = read_from_gdb(buffer);
        gdb_result.append(buffer, read_info.total_read);
    }
    log.log_info("read done!");
    log.log_info("gdb result = \n" + gdb_result);
    return gdb_result;
}

READ_INFO GdbController::read_from_gdb(char *buf) {
    bool bSuccess = false;
    DWORD read_cnt;
    bSuccess = ReadFile( gdb_std_out_rd, buf, BUFFER_SIZE, &read_cnt, nullptr);
    if( ! bSuccess) {
        std::runtime_error("Read From Child Failed\n");
    }
    log.log_info("read successful ! ");
    // 创建一个READ_INFO
    READ_INFO read_info;
    read_info.total_read = read_cnt;
    read_info.has = false;
    DWORD available_cnt;
    read_info.has = ! read_end(buf, read_cnt - 1);
    // 放弃本轮cpu
    std::this_thread::yield();
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    // n次检查
    int count = 5;
    while (count > 0) {
        PeekNamedPipe(gdb_std_out_rd, nullptr, 0, nullptr, &available_cnt, nullptr);
        if (available_cnt != 0) {
            read_info.has = true;
            log.log_info("more info left, need to read again!");
            return read_info;
        }
        count -= 1;
    }
    // n次检查后依然没有数据, 则表示read_end判断有误, 遇到了某些特殊情况
    // read_info.has = false;
    return read_info;
}

DWORD GdbController::write_to_gdb(const char *buf, const DWORD write_cnt) {
    bool bSuccess = false;
    char chBuf[BUFSIZE];
    DWORD read_cnt;
    int i;
    for(i = 0; i < write_cnt; i++){
        chBuf[i]=buf[i];
    }
    chBuf[i++]='\n';

    bSuccess = WriteFile(gdb_std_in_wr, chBuf, i, &read_cnt, nullptr);
    if ( ! bSuccess ) {
        // 用errno增加错误原因
        std::runtime_error("Write To Child Failed\n");
    }
    return read_cnt;
}

/**
 * GDB MI Syntax
 * https://sourceware.org/gdb/current/onlinedocs/gdb.html/GDB_002fMI-Output-Syntax.html#GDB_002fMI-Output-Syntax
 */
ExecuteResult GdbController::parse_gdb_output(const std::string& gdb_result, const GdbInstruction& gdbInst) {
    auto s = trim(gdb_result);
    // 按照换行符切割
    auto lines = split(s, '\n');
    // 如果最后一个元素是(gdb), 移除它
    if (!lines.empty() && ends_with(lines.back(), "(gdb) ")) {
        lines.pop_back();
    }

    ExecuteResult result;
    result.operation = gdbInst.operation;
    CPP_GDB_INFO cpp_gdb_info;

    for (const auto& line : lines) {
        if (starts_with(line, "^")) { // 结果记录
            if (starts_with(line, "^done")) {
                cpp_gdb_info.status = "done";
                // 解析更多详细信息...
            } else if (starts_with(line, "^running")) {
                cpp_gdb_info.status = "running";
            } else if (starts_with(line, "^error")) {
                cpp_gdb_info.status = "error";
                // 提取错误信息...
                auto msg = extract_error(line);
            } else if (starts_with(line, "^exit")) {
                cpp_gdb_info.status = "exit";
            }
            cpp_gdb_info.result_record += extract_text(line);
        } else if (starts_with(line, "*")) { // 异步记录
            if (starts_with(line, "*stopped")) {
                cpp_gdb_info.stopped_reason = extract_text(line); // 自定义函数提取原因
            }
        } else if (starts_with(line, "@")) { // 流输出
            if (starts_with(line, "@console")) {
                cpp_gdb_info.console_output = extract_text(line); // 自定义函数提取输出
            }
        } else if (starts_with(line, "~")) { // 普通文本输出
            result.has_result = true;
            result.result += extract_text(line); // 自定义函数提取文本
        } else if (starts_with(line, "&")) {
            cpp_gdb_info.log_output += extract_text(line);
        }
    }

    result.success = true;
    result.more_info = cpp_gdb_info.to_json().dump();
    return result;
}

std::string GdbController::extract_error(std::string line) {
    auto s = line.substr(11);
    if (s[0] == '\"') s = s.substr(1, s.length() - 2);
    return s;
}

string GdbController::extract_text(const string &basicString) {
    auto s = basicString.substr(1);
    if (s[0] == '\"') s = s.substr(1, s.length() - 2);
    return s;
}

void GdbController::io_with_gdb() {
    // 循环读取 GDB 输出并允许用户输入命令
    while (true) {
        log.log_info("Enter GDB Command: -------------------------------\n");
        auto gdb_command = get_command();
        log.log_info("user input command: " + gdb_command.to_string());
        auto user_command = gdb_command.gdbCommand;

        // 如果用户输入 exit，则退出循环
        if (is_finish(user_command)) {
            write_to_gdb("quit", 4);
            auto r = ExecuteResult::success_no_result("NULL");
            show_gdb_output(r);
            break;
        }

        // 向 GDB 发送命令
        log.log_info("send command to gdb: " + user_command);
        write_to_gdb((user_command).c_str(), user_command.length());

        // 读取 GDB 的输出
        log.log_info("start to read result from gdb");
        auto gdb_result = read_and_handle_from_gdb();

        // 解析 GDB 输出
        ExecuteResult r = parse_gdb_output(gdb_result, gdb_command);

        log.log_info("execute result = \n" + r.to_json() + "\n");

        show_gdb_output(r);
    }
}

bool GdbController::read_end(char *buf, int end) {
    // 判断最后几个字符是不是(gdb)
    char check[] = {'(', 'g', 'd', 'b', ')', ' ', '\n'};
    int round = sizeof (check) - 1;
    for (int i = end; i >= 0 && round >= 0; i--) {
        if (check[round] != buf[i]) {
            return false;
        }
        round -= 1;
    }
    return round == -1;
}

void GdbController::create_gdb_process() {
    PROCESS_INFORMATION piProcInfo; // 存储新进程的信息
    STARTUPINFO siStartInfo;        // 决定如何控制新进程
    bool bSuccess = false;

    // 初始化PROCESS_INFORMATION结构体
    ZeroMemory(&piProcInfo, sizeof(PROCESS_INFORMATION));

    // 初始化STARTUPINFO结构体，设置STDIN和STDOUT句柄用于重定向
    ZeroMemory(&siStartInfo, sizeof(STARTUPINFO));
    siStartInfo.cb = sizeof(STARTUPINFO); // 设置结构体大小
    siStartInfo.hStdError = gdb_std_out_wr; // 错误输出重定向
    siStartInfo.hStdOutput = gdb_std_out_wr; // 标准输出重定向
    siStartInfo.hStdInput = gdb_std_in_rd;   // 标准输入重定向
    siStartInfo.dwFlags |= STARTF_USESTDHANDLES; // 标记使用标准句柄

    // 构建启动GDB的命令行
    // std::string gdbCommand = R"(E:\mingw-2\mingw64\bin\gdb.exe E:\java_code\lc-test\cache\debug\cpp\solution.exe --interpreter=mi2)";
    std::string gdbCommand = this->gdb_path + " " + this->exe_path + " " + this->gdb_param;
    // 创建一个可修改的副本，因为CreateProcess需要一个可修改的字符串
    char mutableCmd[1024];
    strcpy_s(mutableCmd, sizeof(mutableCmd), gdbCommand.c_str());

    // 创建子进程
    bSuccess = CreateProcess(nullptr,
                             mutableCmd,        // 命令行
                             nullptr,             // 进程安全属性
                             nullptr,             // 主线程安全属性
                             TRUE,             // 句柄是否继承
                             0,                // 创建标志
                             nullptr,             // 使用父进程环境
                             nullptr,             // 使用父进程当前目录
                             &siStartInfo,     // 指向STARTUPINFO的指针
                             &piProcInfo);     // 接收PROCESS_INFORMATION的指针

    // 如果创建失败，抛出异常
    if (!bSuccess)
        throw std::runtime_error("CreateProcess Error");
    else {
        // 关闭子进程及其主线程的句柄
        CloseHandle(piProcInfo.hProcess);
        CloseHandle(piProcInfo.hThread);

        // 关闭不再需要的标准输入输出管道句柄
        // 如果不关闭，可能无法正确识别子进程结束
        CloseHandle(gdb_std_out_wr);
        CloseHandle(gdb_std_in_rd);
    }
}

#endif //CPP_GDBCONTROLLER_H
