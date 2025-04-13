//
// Created by 25080 on 2025/1/31.
//
#include "CppDebugServer.h"
#include "StdGdbController.h"
#include "InstSource.h"
#include "WebGdbController.h"
#include "leetcode.h"

int charToInt(const char* str) {
    int num = 0;
    for (int i = 0; str[i] != '\0'; ++i) {
        if (str[i] >= '0' && str[i] <= '9') {
            num = num * 10 + (str[i] - '0');
        } else {
            throw std::invalid_argument("Invalid input string for conversion to int");
        }
    }
    return num;
}

int main(int argc, char* argv[]) {
    int port = charToInt(argv[1]);
    char* std_log_path = argv[2];
    char* std_err_path = argv[3];
    char* gdb_path = argv[4];
    char* solution_exe_path = argv[5];

    LogHelper log(std_log_path, std_err_path);
    log.log_info("welcome to feigebuge's cpp server! please enjoy the coding pleasure!");
    DebugServer server(log, port);
    log.log_info("server will start on port = " + std::to_string(port));
    std::thread cpp_web_server([&server]() {
        server.start();
    });

    auto gdb = WebGdbController(gdb_path, solution_exe_path, R"(--interpreter=mi2)", log);
    gdb.start_gdb();
    cpp_web_server.join();
    log.log_info("server stopped... debug finish...");
}