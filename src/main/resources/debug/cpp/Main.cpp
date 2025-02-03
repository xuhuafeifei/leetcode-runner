//
// Created by 25080 on 2025/1/31.
//
#include "CppDebugServer.h"
#include "StdGdbController.h"
#include "InstSource.h"
#include "WebGdbController.h"

int main() {
    LogHelper log("E:\\java_code\\lc-test\\cache\\debug\\cpp\\log\\std_info.log", "E:\\java_code\\lc-test\\cache\\debug\\cpp\\log\\std_err.log");
    log.log_info("welcome to feigebuge's cpp server! please enjoy the coding pleasure!");
    int port = 8080;
    DebugServer server(log, port);
    log.log_info("server will start on port = " + std::to_string(port));
    std::thread cpp_web_server([&server]() {
        server.start();
    });

    auto gdb = WebGdbController(R"(E:\mingw-2\mingw64\bin\gdb.exe)", R"(E:\java_code\lc-test\cache\debug\cpp\solution.exe)", R"(--interpreter=mi2)", log);
    gdb.start_gdb();
    cpp_web_server.join();
    log.log_info("server stopped... debug finish...");
}