class ListNode:
    def __init__(self, val=0, x=None):
        self.val = val
        self.next = x

    def __human_visible__(self):
        # 迭代
        head = self
        res = '['
        while head:
            res += str(head.val) + ','
            head = head.next
        if res[-1] == ',':
            res = res[:-1] + ']'
        return res