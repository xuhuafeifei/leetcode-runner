//
// Created by 25080 on 2025/1/30.
//

#ifndef CPP_GDBCONTROLLER_H
#define CPP_GDBCONTROLLER_H

#include <windows.h>
#include <tchar.h>
#include <iostream>
#include <strsafe.h>

#include <string>
#include <thread>

#define BUFSIZE 128
#define BUFFER_SIZE 1024 * 2

typedef struct READ_INFO {
    DWORD total_read;
    bool has;
} READ_INFO;

class GdbController {
public:
    GdbController(std::string gdb_path, std::string exe_path, std::string gdb_param);
    void start_gdb();
private:
    std::string gdb_path;
    std::string exe_path;
    std::string gdb_param;
protected:
    char buffer[BUFFER_SIZE];

    HANDLE gdb_std_in_rd = nullptr;
    HANDLE gdb_std_in_wr = nullptr;
    HANDLE gdb_std_out_rd = nullptr;
    HANDLE gdb_std_out_wr = nullptr;
protected:
    virtual std::string get_command() = 0;
    virtual void show_gdb_output(std::string res) = 0;

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
};

GdbController::GdbController(std::string gdb_path, std::string exe_path, std::string gdb_param):
        gdb_path(gdb_path), exe_path(exe_path), gdb_param(gdb_param) {};

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
        read_info = read_from_gdb(buffer);
        gdb_result.append(buffer, read_info.total_read);
    }
    return gdb_result;
}

READ_INFO GdbController::read_from_gdb(char *buf) {
    bool bSuccess = false;
    DWORD read_cnt;
    bSuccess = ReadFile( gdb_std_out_rd, buf, BUFSIZE, &read_cnt, nullptr);
    if( ! bSuccess) {
        std::runtime_error("Read From Child Failed\n");
    }
    // 创建一个READ_INFO
    READ_INFO read_info;
    read_info.total_read = read_cnt;
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
            return read_info;
        }
        count -= 1;
    }
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

void GdbController::io_with_gdb() {
    // 循环读取 GDB 输出并允许用户输入命令
    while (true) {
        std::cout << "Enter GDB command: ";
        std::string user_command;
        std::getline(std::cin, user_command);

        // 如果用户输入 exit，则退出循环
        if (is_finish(user_command)) break;

        // 向 GDB 发送命令
        write_to_gdb((user_command).c_str(), user_command.length());

        // 读取 GDB 的输出
        auto gdb_result = read_and_handle_from_gdb();

        std::cout << gdb_result << std::endl;
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
