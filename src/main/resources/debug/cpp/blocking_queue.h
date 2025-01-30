//
// Created by 25080 on 2025/1/30.
//

#ifndef CPP_BLOCKING_QUEUE_H
#define CPP_BLOCKING_QUEUE_H

#include <queue>
#include <mutex>
#include <condition_variable>

template<typename T>
class BlockingQueue {
public:
    // 添加元素到队列，线程安全
    void push(const T& value) {
        std::lock_guard<std::mutex> lock(mtx_);
        queue_.push(value);
        cv_.notify_one(); // 通知等待的线程
    }

    // 从队列中取出元素，如果队列为空则阻塞直到有元素可用
    T pop() {
        std::unique_lock<std::mutex> lock(mtx_);
        cv_.wait(lock, [this]{ return !queue_.empty(); }); // 等待直到队列非空
        T value = queue_.front();
        queue_.pop();
        return value;
    }

private:
    std::queue<T> queue_;
    mutable std::mutex mtx_;
    std::condition_variable cv_;
};

#endif //CPP_BLOCKING_QUEUE_H
