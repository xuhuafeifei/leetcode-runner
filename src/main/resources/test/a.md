#### 方法一：质数筛

**思路与算法**

特殊数字首先是一个平方数，并且除去自身和 $1$ 之后的另一个因子一定是一个质数。这是因为：

- 因子一般是成双成对的，若一个数字有奇数个因子，那么该数一定是平方数。
- 该数除去自身和 $1$ 仅有一个因子，因此该因子一定是质数。

因此，我们可以在 $[1, \sqrt r]$ 的范围内遍历所有质数（使用质数筛，具体方法可以参考题解 [204. 计数质数](https://leetcode.cn/problems/count-primes/solutions/507273/ji-shu-zhi-shu-by-leetcode-solution/)），然后将它们的平方从 $[l, r]$ 的范围中 去除即可。

由于 $r$ 的范围不超过 $10^9$，因此质数的遍历范围不超过 $31622$，而使用很简单的埃氏筛（复杂度为 $O(n\log n\log n)$，其中 $n$ 为质数遍历范围）就可以轻松通过本题。

**代码**

```C++ [sol1-C++]
class Solution {
public:
    int nonSpecialCount(int l, int r) {
        int n = sqrt(r);
        vector<int> v(n + 1);
        int res = r - l + 1;
        for (int i = 2; i <= n; i++) {
            if (v[i] == 0) {
                if (i * i >= l && i * i <= r) {
                    res--;
                }
                for (int j = i * 2; j <= n; j += i) {
                    v[j] = 1;
                }
            }
        }
        return res;
    }
};
```

```Java [sol1-Java]
class Solution {
    public int nonSpecialCount(int l, int r) {
        int n = (int) Math.sqrt(r);
        int[] v = new int[n + 1];
        int res = r - l + 1;
        for (int i = 2; i <= n; i++) {
            if (v[i] == 0) {
                if (i * i >= l && i * i <= r) {
                    res--;
                }
                for (int j = i * 2; j <= n; j += i) {
                    v[j] = 1;
                }
            }
        }
        return res;
    }
}
```

```C# [sol1-C#]
public class Solution {
    public int NonSpecialCount(int l, int r) {
        int n = (int) Math.Sqrt(r);
        int[] v = new int[n + 1];
        int res = r - l + 1;
        for (int i = 2; i <= n; i++) {
            if (v[i] == 0) {
                if (i * i >= l && i * i <= r) {
                    res--;
                }
                for (int j = i * 2; j <= n; j += i) {
                    v[j] = 1;
                }
            }
        }
        return res;
    }
}
```

```Python [sol1-Python3]
class Solution:
    def nonSpecialCount(self, l: int, r: int) -> int:
        n = int(math.sqrt(r))
        v = [0] * (n + 1)
        res = r - l + 1
        for i in range(2, n + 1):
            if v[i] == 0:
                if l <= i * i <= r:
                    res -= 1
                for j in range(i * 2, n + 1, i):
                    v[j] = 1
        return res
```

```Rust [sol1-Rust]
impl Solution {
    pub fn non_special_count(l: i32, r: i32) -> i32 {
        let n = (r as f64).sqrt() as usize;
        let mut v = vec![0; n + 1];
        let mut res = r - l + 1;

        for i in 2..=n {
            if v[i] == 0 {
                let square = (i * i) as i32;
                if square >= l && square <= r {
                    res -= 1;
                }
                for j in (i * 2..=n).step_by(i) {
                    v[j] = 1;
                }
            }
        }
        res
    }
}
```

```Go [sol1-Go]
func nonSpecialCount(l int, r int) int {
    n := int(math.Sqrt(float64(r)))
        v := make([]int, n + 1)
        res := r - l + 1
        for i := 2; i <= n; i++ {
                if v[i] == 0 {
                        if i * i >= l && i * i <= r {
                                res--
                        }
                        for j := i * 2; j <= n; j += i {
                                v[j] = 1
                        }
                }
        }
        return res
}
```

```C [sol1-C]
int nonSpecialCount(int l, int r) {
    int n = (int)sqrt(r);
    int v[n + 1];
    int res = r - l + 1;
    for (int i = 0; i <= n; i++) {
        v[i] = 0;
    }
    for (int i = 2; i <= n; i++) {
        if (v[i] == 0) {
            if (i * i >= l && i * i <= r) {
                res--;
            }
            for (int j = i * 2; j <= n; j += i) {
                v[j] = 1;
            }
        }
    }
    return res;
}
```

```JavaScript [sol1-JavaScript]
var nonSpecialCount = function(l, r) {
    const n = Math.floor(Math.sqrt(r));
    const v = new Array(n + 1).fill(0);
    let res = r - l + 1;
    for (let i = 2; i <= n; i++) {
        if (v[i] === 0) {
            if (i * i >= l && i * i <= r) {
                res--;
            }
            for (let j = i * 2; j <= n; j += i) {
                v[j] = 1;
            }
        }
    }
    return res;
};
```

```TypeScript [sol1-TypeScript]
function nonSpecialCount(l: number, r: number): number {
    const n = Math.floor(Math.sqrt(r));
    const v = new Array(n + 1).fill(0);
    let res = r - l + 1;
    for (let i = 2; i <= n; i++) {
        if (v[i] === 0) {
            if (i * i >= l && i * i <= r) {
                res--;
            }
            for (let j = i * 2; j <= n; j += i) {
                v[j] = 1;
            }
        }
    }
    return res;
};
```

**复杂度分析**

- 时间复杂度：$O(n\log\log n)$，其中 $n$ 为 $\sqrt r$。

- 空间复杂度：$O(n)$。