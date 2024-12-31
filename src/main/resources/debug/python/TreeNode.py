from binarytree import build


class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

    def __human_visible__(self):
        head = self
        # 统计二叉树高度
        def get_height(root):
            if root is None:
                return 0
            return max(get_height(root.left), get_height(root.right)) + 1
        h = get_height(head)
        # 层序遍历
        q = [head]
        ans = []
        for i in range(2 ** h - 1):
            cur = q.pop(0)
            if cur == None:
                ans.append(None)
                q.append(None)
                q.append(None)
                continue
            ans.append(cur.val)
            q.append(cur.left if cur.left else None)
            q.append(cur.right if cur.right else None)
        return str(build(ans))

if __name__ == '__main__':
    yzzphkfzxf = str("[1,4,4,null,2,2,null,1,null,6,8,null,null,null,null,1,3]").strip()
    # yzzphkfzxf = str("[1,4,4,null]").strip()
    tree = build([1, 4, 4, None, 2, 2, None, None, None, 1, None, 6, 8, None, None, None, None, None, None, 1, 3])
    print(tree)
    a1 = None
    if yzzphkfzxf != "[]":
        # 把collect变为数组
        split = [
            None if e == "null" else int(e)
            for e in yzzphkfzxf.replace("[", "").replace("]", "").split(",")
        ]

        i = 0
        a1 = TreeNode(split[i])
        i += 1

        q = [a1]

        while q:
            node = q.pop(0)
            # 添加它的左节点
            if i < len(split):
                if split[i] is not None and split[i] != "null":
                    node.left = TreeNode(split[i])
                    q.append(node.left)
                i += 1
            # 添加右节点
            if i < len(split):
                if split[i] is not None and split[i] != "null":
                    node.right = TreeNode(split[i])
                    q.append(node.right)
                i += 1
    print(a1.__human_visible__())