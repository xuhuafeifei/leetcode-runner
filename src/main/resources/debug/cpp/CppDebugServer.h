//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_CPPDEBUGSERVER_H
#define CPP_CPPDEBUGSERVER_H


#include "httpclient/httplib.h"
using namespace httplib;

#include <iostream>
#include "nlohmann/json.hpp"
#include "LogHelper.h"
#include "WebGdbController.h"

using json = nlohmann::json;

class DebugServer {
public:
    DebugServer(LogHelper& log, const int port) : log(log), port(port) {
    }
public:
    void start();
    inline void stop() {
        stop_flag = true;
    }
    inline bool is_stop() const {
        return stop_flag;
    }
private:
    bool stop_flag = false;
    int port;
    LogHelper log;
};

void DebugServer::start() {
    Server svr;

    svr.Post("/", [this](const Request &req, Response &res) {
        auto body = req.body;

        auto j = json::parse(body);

        if (j["gdbCommand"] == "quit") {
            InstSource::inst_push({"NULL", "quit"});
            this->stop();
        } else {
            // 写入InstSource
            InstSource::inst_push({j["operation"], j["gdbCommand"]});
        }

        auto r = InstSource::gdb_result_pop();
        std::string response = r.to_json(); // 将 json 对象转换为字符串

        res.set_content(response, "text/plain");
    });

    /**
     * 监控线程, 监控web服务是否结束. 当web接收到quit指令后, 会调用stop()函数, 为web服务打上标记位
     * 监控线程会定时检查web服务是否结束, 如果结束, 则终止web服务, 并退出监控线程
     */
    std::thread watch_thread([&svr, this]() {
        while (true) {
            if (this->is_stop()) {
                svr.stop();
                return;
            }
            // 睡眠
            std::this_thread::sleep_for(std::chrono::milliseconds(300));
        }
    });

    svr.listen("localhost", this->port);

    watch_thread.join();
}
#endif //CPP_CPPDEBUGSERVER_H
