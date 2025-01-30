//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_STDGDBCONTROLLER_H
#define CPP_STDGDBCONTROLLER_H

#include "GdbController.h"


class StdGdbController : public GdbController {
public:
    StdGdbController(std::string gdb_path, std::string exe_path, std::string gdb_param):
        GdbController(gdb_path, exe_path, gdb_param) {
    }
protected:
    std::string get_command() {
        std::cout << "Enter GDB command: ";
        std::string user_command;
        std::getline(std::cin, user_command);
        return user_command;
    }

    void show_gdb_output(std::string res) {
        std::cout << res << std::endl;
    }
};


#endif //CPP_STDGDBCONTROLLER_H
