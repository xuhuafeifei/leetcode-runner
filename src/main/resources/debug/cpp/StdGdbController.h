//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_STDGDBCONTROLLER_H
#define CPP_STDGDBCONTROLLER_H

#include "GdbController.h"

#include <utility>


class StdGdbController : public GdbController {
public:
    StdGdbController(std::string gdb_path, std::string exe_path, std::string gdb_param, LogHelper& _log):
        GdbController(std::move(gdb_path), std::move(exe_path), std::move(gdb_param), _log) {
    }
protected:
    GdbInstruction get_command() override {
        log.log_info("Enter GDB command: ");
        std::string user_command;
        std::getline(std::cin, user_command);
        return {"NULL", user_command};
    }

    void show_gdb_output(ExecuteResult& res) override {
        log.log_info("GDB output: " + res.to_json());
    }
};


#endif //CPP_STDGDBCONTROLLER_H
