//
// Created by 25080 on 2025/1/31.
//
#pragma once
#ifndef CPP_INSTSOURCE_H
#define CPP_INSTSOURCE_H

#include "BlockingQueue.h"
#include "ExecuteResult.h"
#include "GdbInstruction.h"

class InstSource {
public:
    static void inst_push(const GdbInstruction& inst) {
        inst_queue.push(inst);
    }
    static GdbInstruction inst_pop() {
        return inst_queue.pop();
    }
    static void gdb_result_push(const ExecuteResult& r) {
        gdb_result_queue.push(r);
    }
    static ExecuteResult gdb_result_pop() {
        return gdb_result_queue.pop();
    }
private:
    static BlockingQueue<GdbInstruction> inst_queue;
    static BlockingQueue<ExecuteResult> gdb_result_queue;
};

// 初始化inst_queue
BlockingQueue<GdbInstruction> InstSource::inst_queue;
// 初始化gdb_result_queue
BlockingQueue<ExecuteResult> InstSource::gdb_result_queue;

#endif //CPP_INSTSOURCE_H
