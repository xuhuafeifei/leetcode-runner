# Common imports for LeetCode problems
from typing import List, Optional, Tuple, Dict, Set
from collections import defaultdict, Counter, deque, namedtuple, OrderedDict
from heapq import heappush, heappop, heapify
from functools import lru_cache, cache
from itertools import permutations, combinations, product, accumulate
from bisect import bisect_left, bisect_right
from ListNode import ListNode
from TreeNode import TreeNode
import math
import sys
#do not modify or remove anything between start-line and end-line
#lc-start-line
class Solution:
    def __init__(self):
        self.ans = 0

    def isValid(self, board, i, j, n):
        dirs = [(-1, 0),(0,1),(0,-1),(-1,-1),(-1,1)] 
        for dir in dirs:
            n_x, n_y = i + dir[0], j + dir[1]
            while 0 <= n_x < n and 0 <= n_y < n:
                if board[n_x][n_y] == 'Q':
                    return False
                n_x += dir[0]
                n_y += dir[1]
        return True

    def totalNQueens(self, n: int) -> int:
        board = [['.'] * n for _ in range(n)]
        def dfs(i):
            if i == n:
                self.ans += 1
                return
            for j in range(n):
                if self.isValid(board, i, j, n):
                    board[i][j] = 'Q'
                    dfs(i + 1)
                    board[i][j] = '.' 
        dfs(0)
        return self.ans
#lc-end-line

if __name__ == '__main__':
    s = Solution()
    s.totalNQueens(1)