//
// Created by 25080 on 2025/1/31.
//

#ifndef CPP_EXECUTERESULT_H
#define CPP_EXECUTERESULT_H

#include <string>
#include <map>
#include "nlohmann/json.hpp"

using json = nlohmann::json;

class ExecuteResult {
public:
    std::string operation;
    bool success;
    bool has_result;
    std::string result;
    std::string msg;
    int add_line;
    std::string method_name;
    std::string context;
    std::string class_name;
    std::string more_info;

    ExecuteResult(const std::string& operation = "", bool success = false, bool has_result = false,
                  const std::string& result = "", const std::string& msg = "", int add_line = 0,
                  const std::string& method_name = "", const std::string& context = "",
                  const std::string& class_name = "", const std::string& more_info = "")
            : operation(operation), success(success), has_result(has_result), result(result), msg(msg),
              add_line(add_line), method_name(method_name), context(context), class_name(class_name), more_info(more_info) {}

    static ExecuteResult fill_with_frame(ExecuteResult& r, const std::map<std::string, std::string>& frame) {
        // 假设frame包含必要的信息，这里仅作示例
        r.class_name = frame.at("self_class_name");
        r.add_line = std::stoi(frame.at("line_number"));
        r.method_name = frame.at("method_name");
        return r;
    }

    static ExecuteResult successful(const std::string& operation, const std::string& result = "") {
        return ExecuteResult(operation, true, !result.empty(), result);
    }

    static ExecuteResult success_no_result(const std::string& operation) {
        return ExecuteResult(operation, true, false);
    }

    static ExecuteResult fail(const std::string& operation, const std::string& msg = "Some error happens!") {
        return ExecuteResult(operation, false, false, "", msg);
    }

    std::string to_string() const {
        return "ExecuteResult(operation=" + operation + ", success=" + std::to_string(success) +
               ", has_result=" + std::to_string(has_result) + ", result=" + result +
               ", msg=" + msg + ", add_line=" + std::to_string(add_line) +
               ", method_name=" + method_name + ", context=" + context +
               ", class_name=" + class_name + ", more_info=" + more_info + ")";
    }

    std::map<std::string, std::string> to_dict() const {
        return {
                {"operation", operation},
                {"success", std::to_string(success)},
                {"has_result", std::to_string(has_result)},
                {"result", result},
                {"msg", msg},
                {"add_line", std::to_string(add_line)},
                {"method_name", method_name},
                {"context", context},
                {"class_name", class_name},
                {"more_info", more_info}
        };
    }

    std::string to_json() const {
        json j;
        j["operation"] = operation;
        j["success"] = success;
        j["has_result"] = has_result;
        j["result"] = result;
        j["msg"] = msg;
        j["add_line"] = add_line;
        j["method_name"] = method_name;
        j["context"] = context;
        j["class_name"] = class_name;
        j["more_info"] = more_info;

        return j.dump();
    }
};

#endif //CPP_EXECUTERESULT_H
