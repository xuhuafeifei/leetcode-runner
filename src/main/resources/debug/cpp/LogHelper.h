//
// Created by 25080 on 2025/1/31.
//
#pragma once

#ifndef CPP_LOGHELPER_H
#define CPP_LOGHELPER_H
#include <iostream>

class LogHelper {
private:
    void log(std::ostream& out, const std::string& msg) {
        out.write(msg.data(), msg.size());
        out.write("\n", 1);
        out.flush();
    }
public:
    LogHelper() {

    }
    void log_stdout(const std::string& msg) {
        log(std::cout, msg);
    }

    void log_stderr(const std::string& msg) {
        log(std::cerr, msg);
    }

    void log_info(const std::string& msg) {
        log(std::cout, msg);
    }
};

#endif //CPP_LOGHELPER_H
