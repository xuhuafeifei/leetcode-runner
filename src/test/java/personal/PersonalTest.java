package personal;

import com.xhf.leetcode.plugin.model.LeetcodeUserProfile;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.junit.Test;

public class PersonalTest {

    @Test
    public void test() {
        String a = "{\n"
            + "                \"userSlug\": \"bu-chuan-nei-ku-d\",\n"
            + "                \"realName\": \"\\u98de\\u54e5\\u4e0d\\u9e3d\",\n"
            + "                \"aboutMe\": \"\",\n"
            + "                \"asciiCode\": \"\",\n"
            + "                \"userAvatar\": \"https://assets.leetcode.cn/aliyun-lc-upload/users/bu-chuan-nei-ku-d/avatar_1749806465.png\",\n"
            + "                \"gender\": \"1\",\n"
            + "                \"websites\": [],\n"
            + "                \"skillTags\": [],\n"
            + "                \"ipRegion\": \"\\u4e0a\\u6d77\\u5e02\",\n"
            + "                \"birthday\": \"07/06/2003\",\n"
            + "                \"location\": \"0%0\",\n"
            + "                \"useDefaultAvatar\": false,\n"
            + "                \"certificationLevel\": \"NORMAL\",\n"
            + "                \"github\": \"https://github.com/xuhuafeifei\",\n"
            + "                \"school\": {\n"
            + "                    \"schoolId\": \"m5mzg\",\n"
            + "                    \"logo\": \"https://assets.leetcode-cn.com/aliyun-lc-upload/schools/4329eaf6-d8e6-42fb-b61b-19f755fd0c81.jpg\",\n"
            + "                    \"name\": \"\\u592a\\u539f\\u7406\\u5de5\\u5927\\u5b66\"\n"
            + "                },\n"
            + "                \"company\": null,\n"
            + "                \"job\": \"\",\n"
            + "                \"globalLocation\": {\n"
            + "                    \"country\": \"\\u4e2d\\u56fd\",\n"
            + "                    \"province\": \"\\u5c71\\u897f\",\n"
            + "                    \"city\": \"\\u592a\\u539f\",\n"
            + "                    \"overseasCity\": false\n"
            + "                },\n"
            + "                \"socialAccounts\": [],\n"
            + "                \"skillSet\": {\n"
            + "                    \"langLevels\": [\n"
            + "                        {\n"
            + "                            \"langName\": \"cpp\",\n"
            + "                            \"langVerboseName\": \"C++\",\n"
            + "                            \"level\": 3\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"langName\": \"java\",\n"
            + "                            \"langVerboseName\": \"Java\",\n"
            + "                            \"level\": 3\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"langName\": \"mysql\",\n"
            + "                            \"langVerboseName\": \"MySQL\",\n"
            + "                            \"level\": 1\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"langName\": \"scala\",\n"
            + "                            \"langVerboseName\": \"Scala\",\n"
            + "                            \"level\": 1\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"langName\": \"python3\",\n"
            + "                            \"langVerboseName\": \"Python3\",\n"
            + "                            \"level\": 3\n"
            + "                        }\n"
            + "                    ],\n"
            + "                    \"topics\": [\n"
            + "                        {\n"
            + "                            \"slug\": \"array\",\n"
            + "                            \"name\": \"Array\",\n"
            + "                            \"translatedName\": \"\\u6570\\u7ec4\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"backtracking\",\n"
            + "                            \"name\": \"Backtracking\",\n"
            + "                            \"translatedName\": \"\\u56de\\u6eaf\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"binary-search\",\n"
            + "                            \"name\": \"Binary Search\",\n"
            + "                            \"translatedName\": \"\\u4e8c\\u5206\\u67e5\\u627e\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"binary-tree\",\n"
            + "                            \"name\": \"Binary Tree\",\n"
            + "                            \"translatedName\": \"\\u4e8c\\u53c9\\u6811\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"bit-manipulation\",\n"
            + "                            \"name\": \"Bit Manipulation\",\n"
            + "                            \"translatedName\": \"\\u4f4d\\u8fd0\\u7b97\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"breadth-first-search\",\n"
            + "                            \"name\": \"Breadth-First Search\",\n"
            + "                            \"translatedName\": \"\\u5e7f\\u5ea6\\u4f18\\u5148\\u641c\\u7d22\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"combinatorics\",\n"
            + "                            \"name\": \"Combinatorics\",\n"
            + "                            \"translatedName\": \"\\u7ec4\\u5408\\u6570\\u5b66\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"counting\",\n"
            + "                            \"name\": \"Counting\",\n"
            + "                            \"translatedName\": \"\\u8ba1\\u6570\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"database\",\n"
            + "                            \"name\": \"Database\",\n"
            + "                            \"translatedName\": \"\\u6570\\u636e\\u5e93\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"depth-first-search\",\n"
            + "                            \"name\": \"Depth-First Search\",\n"
            + "                            \"translatedName\": \"\\u6df1\\u5ea6\\u4f18\\u5148\\u641c\\u7d22\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"design\",\n"
            + "                            \"name\": \"Design\",\n"
            + "                            \"translatedName\": \"\\u8bbe\\u8ba1\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"divide-and-conquer\",\n"
            + "                            \"name\": \"Divide and Conquer\",\n"
            + "                            \"translatedName\": \"\\u5206\\u6cbb\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"dynamic-programming\",\n"
            + "                            \"name\": \"Dynamic Programming\",\n"
            + "                            \"translatedName\": \"\\u52a8\\u6001\\u89c4\\u5212\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"enumeration\",\n"
            + "                            \"name\": \"Enumeration\",\n"
            + "                            \"translatedName\": \"\\u679a\\u4e3e\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"graph\",\n"
            + "                            \"name\": \"Graph\",\n"
            + "                            \"translatedName\": \"\\u56fe\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"greedy\",\n"
            + "                            \"name\": \"Greedy\",\n"
            + "                            \"translatedName\": \"\\u8d2a\\u5fc3\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"hash-table\",\n"
            + "                            \"name\": \"Hash Table\",\n"
            + "                            \"translatedName\": \"\\u54c8\\u5e0c\\u8868\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"heap-priority-queue\",\n"
            + "                            \"name\": \"Heap (Priority Queue)\",\n"
            + "                            \"translatedName\": \"\\u5806\\uff08\\u4f18\\u5148\\u961f\\u5217\\uff09\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"linked-list\",\n"
            + "                            \"name\": \"Linked List\",\n"
            + "                            \"translatedName\": \"\\u94fe\\u8868\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"math\",\n"
            + "                            \"name\": \"Math\",\n"
            + "                            \"translatedName\": \"\\u6570\\u5b66\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"matrix\",\n"
            + "                            \"name\": \"Matrix\",\n"
            + "                            \"translatedName\": \"\\u77e9\\u9635\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"memoization\",\n"
            + "                            \"name\": \"Memoization\",\n"
            + "                            \"translatedName\": \"\\u8bb0\\u5fc6\\u5316\\u641c\\u7d22\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"ordered-set\",\n"
            + "                            \"name\": \"Ordered Set\",\n"
            + "                            \"translatedName\": \"\\u6709\\u5e8f\\u96c6\\u5408\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"prefix-sum\",\n"
            + "                            \"name\": \"Prefix Sum\",\n"
            + "                            \"translatedName\": \"\\u524d\\u7f00\\u548c\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"queue\",\n"
            + "                            \"name\": \"Queue\",\n"
            + "                            \"translatedName\": \"\\u961f\\u5217\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"recursion\",\n"
            + "                            \"name\": \"Recursion\",\n"
            + "                            \"translatedName\": \"\\u9012\\u5f52\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"segment-tree\",\n"
            + "                            \"name\": \"Segment Tree\",\n"
            + "                            \"translatedName\": \"\\u7ebf\\u6bb5\\u6811\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"simulation\",\n"
            + "                            \"name\": \"Simulation\",\n"
            + "                            \"translatedName\": \"\\u6a21\\u62df\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"sliding-window\",\n"
            + "                            \"name\": \"Sliding Window\",\n"
            + "                            \"translatedName\": \"\\u6ed1\\u52a8\\u7a97\\u53e3\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"sorting\",\n"
            + "                            \"name\": \"Sorting\",\n"
            + "                            \"translatedName\": \"\\u6392\\u5e8f\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"stack\",\n"
            + "                            \"name\": \"Stack\",\n"
            + "                            \"translatedName\": \"\\u6808\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"string\",\n"
            + "                            \"name\": \"String\",\n"
            + "                            \"translatedName\": \"\\u5b57\\u7b26\\u4e32\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"topological-sort\",\n"
            + "                            \"name\": \"Topological Sort\",\n"
            + "                            \"translatedName\": \"\\u62d3\\u6251\\u6392\\u5e8f\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"tree\",\n"
            + "                            \"name\": \"Tree\",\n"
            + "                            \"translatedName\": \"\\u6811\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"trie\",\n"
            + "                            \"name\": \"Trie\",\n"
            + "                            \"translatedName\": \"\\u5b57\\u5178\\u6811\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"two-pointers\",\n"
            + "                            \"name\": \"Two Pointers\",\n"
            + "                            \"translatedName\": \"\\u53cc\\u6307\\u9488\"\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"slug\": \"union-find\",\n"
            + "                            \"name\": \"Union Find\",\n"
            + "                            \"translatedName\": \"\\u5e76\\u67e5\\u96c6\"\n"
            + "                        }\n"
            + "                    ],\n"
            + "                    \"topicAreaScores\": [\n"
            + "                        {\n"
            + "                            \"score\": 49,\n"
            + "                            \"topicArea\": {\n"
            + "                                \"name\": \"\\u6570\\u636e\\u7ed3\\u6784\",\n"
            + "                                \"slug\": \"data-structures\"\n"
            + "                            }\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"score\": 47,\n"
            + "                            \"topicArea\": {\n"
            + "                                \"name\": \"\\u8bbe\\u8ba1\",\n"
            + "                                \"slug\": \"design\"\n"
            + "                            }\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"score\": 42,\n"
            + "                            \"topicArea\": {\n"
            + "                                \"name\": \"\\u57fa\\u7840\\u67b6\\u6784\",\n"
            + "                                \"slug\": \"architecture\"\n"
            + "                            }\n"
            + "                        },\n"
            + "                        {\n"
            + "                            \"score\": 49,\n"
            + "                            \"topicArea\": {\n"
            + "                                \"name\": \"\\u7b97\\u6cd5\",\n"
            + "                                \"slug\": \"algorithms\"\n"
            + "                            }\n"
            + "                        }\n"
            + "                    ]\n"
            + "                }\n"
            + "            }";
        LeetcodeUserProfile leetcodeUserProfile = GsonUtils.fromJson(a, LeetcodeUserProfile.class);
        System.out.println(leetcodeUserProfile.toString());
    }

}
