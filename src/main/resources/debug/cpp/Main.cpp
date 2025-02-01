//
// Created by 25080 on 2025/1/31.
//
#include "CppDebugServer.h"
#include "StdGdbController.h"
#include "InstSource.h"
#include "WebGdbController.h"

int main() {
    LogHelper log;
    DebugServer server(log, 8080);
    std::thread cpp_web_server([&server]() {
        server.start();
    });

    auto gdb = WebGdbController(R"(E:\mingw-2\mingw64\bin\gdb.exe)", R"(E:\java_code\lc-test\cache\debug\cpp\solution.exe)", R"(--interpreter=mi2)", log);
    gdb.start_gdb();
    cpp_web_server.join();
}