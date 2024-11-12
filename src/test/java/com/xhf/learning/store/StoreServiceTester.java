package com.xhf.learning.store;

public class StoreServiceTester {
//    @Test
//    public void testCache() {
//        StoreManager instance = StoreManager.getInstance();
//        List<Question> totalQuestion = LeetcodeClient.getTotalQuestion();
//        instance.addCache(StoreManager.QUESTION_LIST_KEY, totalQuestion);
//
//        instance.addCache(StoreManager.LEETCODE_SESSION_KEY, "123455", false);
//
//        System.out.println(instance.getCacheJson(StoreManager.QUESTION_LIST_KEY));
//
//        System.out.println(instance.getCacheJson(StoreManager.LEETCODE_SESSION_KEY));
//
//        instance.persistCache();
//    }
//
//    public static void main(String[] args) {
//        StoreManager instance = StoreManager.getInstance();
//        List<Question> totalQuestion = LeetcodeClient.getTotalQuestion();
//        instance.addCache(StoreManager.QUESTION_LIST_KEY, totalQuestion);
//
//        instance.addCache(StoreManager.LEETCODE_SESSION_KEY, "123455", false);
//
//        System.out.println(instance.getCacheJson(StoreManager.QUESTION_LIST_KEY));
//
//        System.out.println(instance.getCacheJson(StoreManager.LEETCODE_SESSION_KEY));
//
//        instance.persistCache();
//    }
//
//    @Test
//    public void testJson() {
//        StoreManager.StoreContent storeContent = new StoreManager.StoreContent();
//        List<Question> questionList = LeetcodeClient.getQuestionList(new SearchParams
//                .ParamsBuilder()
//                .setLimit(1)
//                .setSkip(0)
//                .build()
//        );
//        String json = GsonUtils.toJsonStr(questionList);
//        System.out.println(json);
//        storeContent.setContentJson(json);
//
//        String json2 = GsonUtils.toJsonStr(storeContent);
//
//        System.out.println(json2);
//
//        // de-json
//        StoreManager.StoreContent content = GsonUtils.fromJson(json2, StoreManager.StoreContent.class);
//        String content1 = content.getContentJson();
//        System.out.println(content1);
//
//        List<Question> questions = GsonUtils.fromJsonToList(content1, Question.class);
//        System.out.println(questions);
//    }
}
