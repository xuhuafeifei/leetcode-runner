package com.xhf.leetcode.plugin.review.backend.algorithm;

import com.xhf.leetcode.plugin.review.backend.algorithm.result.AlgorithmResult;

/**
 * @author 文艺倾年
 */
public interface Algorithm {

    /**
     * 执行算法计算并返回结果
     * 每个实现该接口的类都必须提供该方法的具体实现，以执行实际的算法逻辑
     * @return AlgorithmResult 返回算法计算的结果，具体类型为AlgorithmResult，包含算法的输出和可能的其他信息
     */
    AlgorithmResult calc();
}
