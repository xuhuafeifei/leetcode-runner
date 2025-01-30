//
// Created by 25080 on 2025/1/31.
//
#include "StdGdbController.h"

int main() {
    StdGdbController gdb(R"(E:\mingw-2\mingw64\bin\gdb.exe)", R"(E:\java_code\lc-test\cache\debug\cpp\solution.exe)", R"(--interpreter=mi2)");
    gdb.start_gdb();
}