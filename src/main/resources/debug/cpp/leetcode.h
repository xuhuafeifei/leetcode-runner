//
// Created by 25080 on 2025/1/29.
//

#ifndef LEETCODE_H
#define LEETCODE_H

#include <iostream>
#include <vector>
#include <unordered_map>
#include <map>
#include <set>
#include <sstream>
#include <unordered_set>
#include <algorithm>
#include <queue>

using namespace std;

// 去除字符串前的空白字符
string ltrim(const string& s) {
    size_t start = s.find_first_not_of(" \t\n\r\f\v");
    return (start == string::npos) ? "" : s.substr(start);
}

// 去除字符串后的空白字符
string rtrim(const string& s) {
    size_t end = s.find_last_not_of(" \t\n\r\f\v");
    return (end == string::npos) ? "" : s.substr(0, end + 1);
}

// 去除字符串前后的空白字符
string trim(const string& s) {
    return rtrim(ltrim(s));
}

// std命名空间
using namespace std;

class ListNode {
public:
    int val;
    ListNode *next;
    ListNode(int x) : val(x), next(nullptr) {}
    ListNode(int x, ListNode *next) : val(x), next(next) {}
};

class TreeNode {
public:
    int val;
    TreeNode *left;
    TreeNode *right;
    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
    TreeNode(int x, TreeNode *left, TreeNode *right) : val(x), left(left), right(right) {}
};

// 函数用于移除字符串s中所有的指定字符 ch
std::string remove_char(const std::string& s, char ch) {
    std::string result;
    // 遍历字符串s的每个字符
    for (char current_char : s) {
        // 如果当前字符不是要移除的字符，则添加到结果字符串中
        if (current_char != ch) {
            result += current_char;
        }
    }
    return result;
}

#endif //LEETCODE_H
