#include "leetcode.h"
class Solution {
public:
    vector<int> twoSum(vector<int>& nums, int target) {
        unordered_map<int, int> hashmap;
        for (int i = 0; i < nums.size(); i++) {
            int complement = target - nums[i];
            if (hashmap.find(complement) != hashmap.end()) {
                return {hashmap[complement], i};
            }
            hashmap[nums[i]] = i;
        }
        return {};
    }
};
int main() {
    Solution solution;
    vector<int> nums = {1, 2, 3, 4, 5};
    int target = 9;
    vector<int> result = solution.twoSum(nums, target);
    return 0;
}