//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_WEBGDBCONTROLLER_H
#define CPP_WEBGDBCONTROLLER_H

#include "GdbController.h"
#include "InstSource.h"

class WebGdbController : public GdbController {
public:
    WebGdbController(std::string gdb_path, std::string exe_path, std::string gdb_param, LogHelper& _log) :
        GdbController(gdb_path, exe_path, gdb_param, _log) {}

protected:
    GdbInstruction get_command() {
        return InstSource::inst_pop();
    }

    void show_gdb_output(ExecuteResult& r) {
        InstSource::gdb_result_push(r);
    }
};

#endif //CPP_WEBGDBCONTROLLER_H
