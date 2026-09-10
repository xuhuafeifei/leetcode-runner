class ListNode:
    def __init__(self, val=0, x=None):
        self.val = val
        self.next = x

    def __human_visible__(self):
        # 快慢指针检测环，防止反转链表调试时死循环
        head = self
        fast = self
        res = '['
        while head:
            res += str(head.val) + ','
            head = head.next
            if fast and fast.next:
                fast = fast.next.next
            if fast and head is fast:
                res += '...,'
                break
        if res[-1] == ',':
            res = res[:-1] + ']'
        return res