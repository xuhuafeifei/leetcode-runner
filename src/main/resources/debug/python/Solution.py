# Common imports for LeetCode problems
from typing import List, Optional, Tuple, Dict, Set
from collections import defaultdict, Counter, deque, namedtuple, OrderedDict
from heapq import heappush, heappop, heapify
from functools import *
import operator
from itertools import permutations, combinations, product, accumulate
from bisect import bisect_left, bisect_right
import math
import sys
from ListNode import ListNode
from TreeNode import TreeNode
#do not modify or remove start-line comment and end-line comment and including this comment
#lc-start-line
class Solution:
    def merge(self, nums1: List[int], m: int, nums2: List[int], n: int) -> None:
        """
        Do not return anything, modify nums1 in-place instead.
        """
        l, r, k = m - 1, n - 1, m + n - 1
        while l >= 0 or r >= 0:
            if l < 0:
                nums1[k] = nums2[r]
                r -= 1
                continue
            if r < 0:
                nums1[k] = nums1[l]
                l -= 1
                continue
            # choose the larger one
            nums1[k] = max(nums1[l], nums2[r])
            if nums2[r] > nums1[l]:
                r -= 1
            else:
                l -= 1
            k -= 1
#lc-end-line