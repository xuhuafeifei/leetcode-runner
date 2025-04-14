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
    LogHelper() = default;
    LogHelper(std::string std_out_path, std::string std_err_path) {
        // 打开文件
        this->std_out_path = std_out_path;
        this->std_err_path = std_err_path;
    }
    void log_stdout(const std::string& msg) {
        std::ofstream outFile(this->std_out_path, std::ios::app | std::ios::out | std::ios::ate);
        log(outFile, msg);
    }

    void log_stderr(const std::string& msg) {
        std::ofstream outFile(this->std_err_path, std::ios::app | std::ios::out | std::ios::ate);
        log(std::cerr, msg);
    }

    void log_info(const std::string& msg) {
        // std::ofstream outFile(this->std_out_path, std::ios::app | std::ios::out | std::ios::ate);
        // TODO: 改回来
        log(std::cout, msg);
    }

private:
    std::string std_out_path;
    std::string std_err_path;
};

#endif //CPP_LOGHELPER_H
