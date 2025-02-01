//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_GDBINSTRUCTION_H
#define CPP_GDBINSTRUCTION_H
#include <string>

class GdbInstruction {
public:
    std::string operation;
    std::string gdbCommand;

    std::string to_string() const {
        return operation + " " + gdbCommand;
    }
};

#endif //CPP_GDBINSTRUCTION_H
