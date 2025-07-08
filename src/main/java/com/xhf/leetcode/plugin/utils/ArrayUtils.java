package com.xhf.leetcode.plugin.utils;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class ArrayUtils {

    public static int binarySearch(int[] arr, int target) {
        int lef = 0, rig = arr.length - 1, mid;
        while (lef <= rig) {
            mid = (lef + rig) / 2;
            var k = arr[mid];
            if (k < target) {
                lef = mid + 1;
            } else if (k > target) {
                rig = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
}
