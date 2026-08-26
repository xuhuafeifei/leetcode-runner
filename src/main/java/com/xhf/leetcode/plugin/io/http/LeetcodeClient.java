package com.xhf.leetcode.plugin.io.http;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.CodeSubmitEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.Article;
import com.xhf.leetcode.plugin.model.CalendarSubmitRecord;
import com.xhf.leetcode.plugin.model.GraphqlReqBody;
import com.xhf.leetcode.plugin.model.HttpRequest;
import com.xhf.leetcode.plugin.model.HttpResponse;
import com.xhf.leetcode.plugin.model.LeetcodeUserProfile;
import com.xhf.leetcode.plugin.model.Question;
import com.xhf.leetcode.plugin.model.RunCode;
import com.xhf.leetcode.plugin.model.RunCodeResult;
import com.xhf.leetcode.plugin.model.Solution;
import com.xhf.leetcode.plugin.model.Submission;
import com.xhf.leetcode.plugin.model.SubmissionDetail;
import com.xhf.leetcode.plugin.model.SubmitCodeResult;
import com.xhf.leetcode.plugin.model.TodayRecord;
import com.xhf.leetcode.plugin.model.UserCalendar;
import com.xhf.leetcode.plugin.model.UserContestRanking;
import com.xhf.leetcode.plugin.model.UserProgressQuestionList;
import com.xhf.leetcode.plugin.model.UserQuestionProgress;
import com.xhf.leetcode.plugin.model.UserStatus;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.groovy.util.Maps;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.jetbrains.annotations.NotNull;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
// 订阅clearCache事件, 当该事件触发后, 清除cookie和ql(List<Question>, 是题目数据的二级缓存)
@LCSubscriber(events = {ClearCacheEvent.class, CodeSubmitEvent.class})
public class LeetcodeClient {

    private static boolean first = true;
    private static volatile LeetcodeClient instance;
    private final HttpClient httpClient;
    private final GraphqlClient graphqlClient;
    UserCalendar userCalendar;
    private Project project;
    private UserStatus userStatus;
    private LeetcodeUserProfile leetcodeUserProfile = null;
    private UserQuestionProgress userQuestionProgress;
    private UserContestRanking userContestRanking;
    private UserProgressQuestionList userProgressQuestionList;
    // second cache
    private List<Question> ql;

    private LeetcodeClient(Project project) {
        this.project = project;
        // loadCache
        httpClient = HttpClient.getInstance();
        // 初始化 GraphqlClient，通过桥接共享 Apache HttpClient 的 cookie
        graphqlClient = GraphqlClient.create(
            new ApacheCookieJarBridge(httpClient.getCookieStore()),
            LeetcodeApiUtils.getLeetcodeReqUrl(),
            LeetcodeApiUtils.getLeetcodeUrl()
        );
        LCEventBus.getInstance().register(this);
    }


    // this method used for test
    @Deprecated
    private LeetcodeClient() {
        httpClient = HttpClient.getInstance();
        graphqlClient = GraphqlClient.create(
            new ApacheCookieJarBridge(httpClient.getCookieStore()),
            LeetcodeApiUtils.getLeetcodeReqUrl(),
            LeetcodeApiUtils.getLeetcodeUrl()
        );
    }

    public static LeetcodeClient getInstance(Project project) {
        if (instance != null) {
            return instance;
        }
        synchronized (LeetcodeClient.class) {
            if (instance == null) {
                instance = new LeetcodeClient(project);
                instance.loadCache(project);
            }
        }
        return instance;
    }

    @Deprecated // this method used for test
    public static LeetcodeClient getInstanceForTest() {
        instance = new LeetcodeClient();
        return instance;
    }

    /**
     * init instance if it is the first time
     * LeetcodeClient need to load cache, which may take a lot of time.
     * therefore,
     * it should be called in a thread when the plugin is loaded
     *
     * @param project project
     */
    public static void init(Project project) {
        if (first) {
            first = false;
            // ApplicationManager.getApplication().invokeLater(() -> {
            // });
            // 系统主要组件的初始化, 尽量不使用异步的方式处理
            getInstance(project);
        }
    }

    @Subscribe
    public void clearCacheListener(ClearCacheEvent event) {
        clearCookies();
        ql = null;
    }

    private void loadCache(Project project) {
        StoreService storeService = StoreService.getInstance(project);
        String LEETCODE_SESSION = storeService.getCacheJson(StoreService.LEETCODE_SESSION_KEY);
        if (LEETCODE_SESSION == null) {
            return;
        }
        // load cookie
        this.setCookie(new BasicClientCookie2(LeetcodeApiUtils.LEETCODE_SESSION, LEETCODE_SESSION), false);
        // load question
        this.loadQuestionCache();
    }

    private void setCookie(Cookie cookie, boolean needCache) {
        if (cookie.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION)) {
            // need to store cache
            if (needCache) {
                // 加密缓存 v3.6.8引入
                StoreService.getInstance(project).addEncryptCache(StoreService.LEETCODE_SESSION_KEY, cookie.getValue());
            }
        }
        httpClient.setCookie(cookie);
    }

    /**
     * set and persist cookie if cookie name equals to LEETCODE_SESSION, which represent the user info
     */
    public void setCookie(Cookie cookie) {
        setCookie(cookie, true);
    }

    public void setCookies(List<Cookie> cookieList) {
        for (Cookie cookie : cookieList) {
            setCookie(cookie);
        }
    }

    /**
     * 判断登录状态
     *
     * @return boolean
     */
    public boolean isLogin() {
        UserStatus userStatus = queryUserStatus();
        if (userStatus == null) {
            return false;
        }
        return userStatus.getIsSignedIn();
    }

    /**
     * 判断用户是否是VIP会员
     */
    public boolean isPremium() {
        UserStatus userStatus = queryUserStatus();
        if (userStatus == null) {
            return false;
        }
        return userStatus.getIsPremium();
    }

    /**
     * 查询用户状态信息
     *
     * @return UserStatus
     */
    public UserStatus queryUserStatus() {
        if (!httpClient.containsCookie(LeetcodeApiUtils.LEETCODE_SESSION)) {
            return null;
        }
        if (userStatus != null) {
            return userStatus;
        }
        try {
            UserStatus result = graphqlClient.query(
                LeetcodeApiUtils.USER_STATUS_QUERY,
                new java.util.HashMap<>(),
                java.util.Arrays.asList("userStatus"),
                UserStatus.class
            );
            this.userStatus = result;
            return result;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    @Deprecated // leetcode login api is not suitable
    public Boolean login(String username, String password) {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
            .addJsonBody("username", username)
            .addJsonBody("password", password)
            .build();
        HttpResponse httpResponse = httpClient.executePost(httpRequest, project);
        return httpResponse.getStatusCode() == 200 ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean updateQuestionStatusByFqid(String fqid, boolean correctAnswer) {
        if (ql == null) {
            return false;
        }
        for (Question question : ql) {
            if (question.getFrontendQuestionId().equals(fqid)) {
                String status = question.getStatus();
                if (correctAnswer) {
                    question.setStatus("AC");
                } else if (status.equals("NOT_STARTED")) {
                    question.setStatus("TRIED");
                }
                return true;
            }
        }
        // keep first cache is same with second cache
        asyncPersistQuestionCache();
        return false;
    }

    /**
     * 异步更新缓存, 同步1级-2级缓存内容
     */
    private void asyncPersistQuestionCache() {
        new Thread(() -> {
            StoreService.getInstance(project).addCache(StoreService.QUESTION_LIST_KEY, ql, true);
        }).start();
    }

    /**
     * get all questions need a lot of time, therefore, this method will read it from cache.
     * if the cache does not exist, this method will query it from leetcode
     * <p>
     * get data from StoreService need to Gson parse, but data cached in current class no need to parse
     * so to faster query the question, this method will also store the questions in
     * current class to speed up the query process, not only store in StoreService
     * <p>
     * to keep the data consistency, this method will also update the cache in StoreService
     */
    public List<Question> getTotalQuestion() {
        // load cache
        // TODO 增加抖动搜索
        List<Question> ans = loadQuestionCache();
        if (ans != null) {
            return ans;
        }
        ans = queryTotalQuestion();
        // store in first cache
        StoreService.getInstance(project).addCache(StoreService.QUESTION_LIST_KEY, ans, true);
        // store in second cache
        this.ql = ans;
        return ans;
    }

    public @NotNull List<Question> queryTotalQuestion() {
        LogUtils.info("[题目列表] queryTotalQuestion 开始请求...");
        List<Question> ans = new java.util.ArrayList<>();
        boolean flag = true;
        int skip = 0;
        final int limit = 100;
        while (flag) {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("categorySlug", "all-code-essentials");
            variables.put("skip", skip);
            variables.put("limit", limit);
            variables.put("filters", new java.util.HashMap<>());

            try {
                JsonObject jsonObject = graphqlClient.queryForJsonObject(
                    LeetcodeApiUtils.PROBLEM_SET_QUERY,
                    variables
                );
                JsonObject pql = jsonObject.getAsJsonObject("data").getAsJsonObject("problemsetQuestionList");
                if (!pql.get("hasMore").getAsBoolean()) {
                    flag = false;
                }
                JsonArray jsonArray = pql.getAsJsonArray("questions");
                // 手动解析 List<Question>
                List<Question> questions = new java.util.ArrayList<>();
                for (JsonElement element : jsonArray) {
                    questions.add(com.xhf.leetcode.plugin.utils.GsonUtils.fromJson(element, Question.class));
                }
                ans.addAll(questions);
                skip += limit;
            } catch (Exception e) {
                LogUtils.error("[题目列表] queryTotalQuestion 失败: " + e.getMessage(), e);
                break;
            }
        }
        LogUtils.info("[题目列表] queryTotalQuestion 成功，共 " + ans.size() + " 道题");
        return ans;
    }

    private List<Question> loadQuestionCache() {
        if (ql == null) {
            // if the second cache is null, search the first cache
            String questionListJson = StoreService.getInstance(project).getCacheJson(StoreService.QUESTION_LIST_KEY);
            if (StringUtils.isBlank(questionListJson)) {
                return null;
            }
            ql = GsonUtils.fromJsonToList(questionListJson, Question.class);
            return ql;
        } else {
            return ql;
        }
    }

    /**
     * @param params search condition
     */
    public List<Question> getQuestionList(GraphqlReqBody.SearchParams params) {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.PROBLEM_SET_QUERY);
        // build by params
        body.setBySearchParams(params);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
            .setBody(body.toJsonStr())
            .setContentType("application/json")
            .addBasicHeader()
            .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest, project);

        String resp = httpResponse.getBody();

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonObject("data")
            .getAsJsonObject("problemsetQuestionList")
            .getAsJsonArray("questions");

        return GsonUtils.fromJsonArray(jsonArray, Question.class);
    }

    public void clearCookies() {
        httpClient.clearCookies();
    }

    /**
     * query question info, which contains more info such as code snippets, translated content, etc.
     * <p>
     * more important, title slug is required, if not, the code will throw exception
     */
    public String queryQuestionInfoJson(GraphqlReqBody.SearchParams params) {
        if (StringUtils.isBlank(params.getTitleSlug())) {
            throw new RuntimeException("title slug is null ! " + GsonUtils.toJsonStr(params));
        }
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("titleSlug", params.getTitleSlug());
        return graphqlClient.queryForJsonObject(
            LeetcodeApiUtils.QUESTION_CONTENT_QUERY,
            variables
        ).toString();
    }

    /**
     * get random question
     */
    public Question getRandomQuestion(Project project) {
        List<Question> totalQuestion = this.getTotalQuestion();
        StoreService storeService = StoreService.getInstance(project);

        int qId = RandomUtils.nextInt(0, totalQuestion.size() - 1);
        while (storeService.contains(String.valueOf(qId))) {
            qId = RandomUtils.nextInt(0, totalQuestion.size() - 1);
        }
        storeService.addCache(String.valueOf(qId), qId, false);

        return totalQuestion.get(qId);
    }

    private JsonObject fetchTodayQuestionJsonObject() {
        return graphqlClient.queryForJsonObject(
            LeetcodeApiUtils.QUESTION_OF_TODAY_QUERY,
            new java.util.HashMap<>()
        );
    }
    
    public TodayRecord getTodayRecord(Project project) {
        JsonObject jsonObject = fetchTodayQuestionJsonObject();
        JsonElement todayRecord = jsonObject.getAsJsonObject("data").getAsJsonArray("todayRecord").get(0);
        return GsonUtils.fromJson(todayRecord, TodayRecord.class);
    }

    /**
     * get today question
     */
    public Question getTodayQuestion(Project project) {
        JsonObject jsonObject = fetchTodayQuestionJsonObject();
        JsonObject question = jsonObject.getAsJsonObject("data").getAsJsonArray("todayRecord").get(0).getAsJsonObject().getAsJsonObject("question");
        return GsonUtils.fromJson(question, Question.class);
    }


    /**
     * run code by leetcode platform
     */
    public RunCodeResult runCode(RunCode runCodeModel) {
        /* check params */
        if (StringUtils.isBlank(runCodeModel.getQuestionId()) ||
            StringUtils.isBlank(runCodeModel.getLang()) ||
            StringUtils.isBlank(runCodeModel.getDataInput()) ||
            StringUtils.isBlank(runCodeModel.getTypeCode()) ||
            StringUtils.isBlank(runCodeModel.getTitleSlug())
        ) {
            throw new RuntimeException("missing params " + runCodeModel);
        }

        String url = LeetcodeApiUtils.getRunCodeUrl(runCodeModel.getTitleSlug());

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
            .setBody(GsonUtils.toJsonStr(runCodeModel))
            .addHeader("Accept", "application/json")
            .setContentType("application/json")
            .addBasicHeader()
            .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest, project);

        String resp = httpResponse.getBody();

        // get interpret_id
        String interpretId = JsonParser.parseString(resp).getAsJsonObject().get("interpret_id").getAsString();

        resp = checkAndGetLeetcodeAnswer(interpretId);
        return GsonUtils.fromJson(resp, RunCodeResult.class);
    }

    /**
     * submit code
     */
    public SubmitCodeResult submitCode(RunCode runCodeModel) {
        /* check params */
        if (StringUtils.isBlank(runCodeModel.getQuestionId()) ||
            StringUtils.isBlank(runCodeModel.getLang()) ||
            StringUtils.isBlank(runCodeModel.getTypeCode()) ||
            StringUtils.isBlank(runCodeModel.getTitleSlug())
        ) {
            throw new RuntimeException("missing params " + runCodeModel);
        }

        String url = LeetcodeApiUtils.getSubmitCodeUrl(runCodeModel.getTitleSlug());

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
            .setBody(GsonUtils.toJsonStr(runCodeModel))
            .addHeader("Accept", "application/json")
            .setContentType("application/json")
            .addBasicHeader()
            .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest, project);

        String resp = httpResponse.getBody();

        // get submission_id
        String submissionId = JsonParser.parseString(resp).getAsJsonObject().get("submission_id").getAsString();

        resp = checkAndGetLeetcodeAnswer(submissionId);
        return GsonUtils.fromJson(resp, SubmitCodeResult.class);
    }

    /**
     * check leetcode whether ready for provide a run code result for a client to read
     * <p>
     * if a code result is not ready, a client will wait for it until it can be read
     *
     * @return the result of leetcode
     */
    private String checkAndGetLeetcodeAnswer(String id) {
        String url = LeetcodeApiUtils.getSubmissionCheckUrl(id);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url).setContentType("application/json").build();

        HttpResponse httpResponse = httpClient.executeGet(httpRequest, project);

        // check data
        /*
        持续调用checkLeetcodeReady方法, 如果该方法返回false, 则表示数据还未完成准备
        循环调用, 直到Leetcode服务端将数据准备完成
         */
        while (!checkLeetcodeReady(httpResponse)) {
            httpResponse = httpClient.executeGet(httpRequest, project);
        }

        return httpResponse.getBody();
    }

    /**
     * 检测leetcode服务端是否将数据准备完全, 如果准备完成, 则返回true, 否则返回false
     *
     * @param httpResponse resp
     * @return leetcode服务端是否将数据准备完全
     */
    private boolean checkLeetcodeReady(HttpResponse httpResponse) {
        String resp = httpResponse.getBody();
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        /*
         * if the result contains `state` field, that means the answer is not ready yet
         * otherwise, the result is ready
         */
        JsonElement state = jsonObject.get("state");
        if (state == null) {
            return true;
        }
        // 判断字段个数, 目前为止, 大于1就意味着数据准备完成
        // todo:? 大于1, 就一定意味着数据准备完成吗?
        int size = jsonObject.asMap().size();
        // 保险点, 大于2就认为返回的是真是的数据
        return size > 2;
    }

    public List<Solution> querySolutionList(String questionSlug) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("questionSlug", questionSlug);
        variables.put("skip", 0);
        variables.put("first", 30);
        variables.put("orderBy", "DEFAULT");

        try {
            JsonObject jsonObject = graphqlClient.queryForJsonObject(
                LeetcodeApiUtils.SOLUTION_LIST_QUERY,
                variables
            );
            JsonArray edges = jsonObject.getAsJsonObject("data")
                .getAsJsonObject("questionSolutionArticles")
                .getAsJsonArray("edges");

            List<Solution> res = new ArrayList<>(15);
            for (JsonElement element : edges) {
                JsonElement node = element.getAsJsonObject().get("node");
                Solution solution = GsonUtils.fromJson(node, Solution.class);
                res.add(solution);
            }
            return res;
        } catch (Exception e) {
            LogUtils.warn("查询题解失败,返回空题解!");
            LogUtils.warn(e.getMessage());
            return new ArrayList<>();
        }
    }

    public String getSolutionContent(String solutionSlug) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("solutionSlug", solutionSlug);
        try {
            return graphqlClient.queryForJsonObject(
                LeetcodeApiUtils.SOLUTION_CONTENT_QUERY,
                variables
            ).toString();
        } catch (Exception e) {
            LogUtils.error("getSolutionContent failed: " + e.getMessage());
            return "";
        }
    }

    public List<Submission> getSubmissionList(String slug) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("questionSlug", slug);
        variables.put("offset", 0);
        variables.put("limit", 50);
        try {
            return graphqlClient.queryList(
                LeetcodeApiUtils.SUBMISSION_LIST_QUERY,
                variables,
                java.util.Arrays.asList("submissionList", "submissions"),
                Submission.class
            );
        } catch (Exception e) {
            LogUtils.error("getSubmissionList failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public SubmissionDetail getSubmissionDetail(String submissionId) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("submissionId", submissionId);
        return graphqlClient.query(
            LeetcodeApiUtils.SUBMISSION_CONTENT_QUERY,
            variables,
            java.util.Arrays.asList("submissionDetail"),
            SubmissionDetail.class
        );
    }

    @Deprecated
    public String getSubmissionCode(String submissionId) {
        try {
            JsonObject jsonObject = querySubmissionDetailForJsonObject(submissionId);
            return jsonObject.getAsJsonObject("data").getAsJsonObject("submissionDetail").get("code").getAsString();
        } catch (Exception e) {
            LogUtils.error("getSubmissionCode failed: " + e.getMessage());
            return "";
        }
    }
    
    private JsonObject querySubmissionDetailForJsonObject(String submissionId) {
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("submissionId", submissionId);
        return graphqlClient.queryForJsonObject(
            LeetcodeApiUtils.SUBMISSION_CONTENT_QUERY,
            variables
        );
    }



    public void cacheQuestionList(List<Question> totalQuestion) {
        ql = totalQuestion;
        asyncPersistQuestionCache();
    }

    public Article queryArticle(String articleUrl) {
        try {
            String[] urls = articleUrl.split("/");
            String uuid = urls[urls.length - 1];
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("uuid", uuid);
            
            JsonObject jsonObject = graphqlClient.queryForJsonObject(
                LeetcodeApiUtils.ARTICLE_CONTENT_QUERY,
                variables
            );
            
            JsonElement jsonElement = jsonObject.get("data").getAsJsonObject().get("qaQuestion");
            return GsonUtils.fromJson(jsonElement, Article.class);
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    public CalendarSubmitRecord getCalendarSubmitRecord() {
        try {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            return graphqlClient.query(
                LeetcodeApiUtils.CALENDAR_SUBMIT_RECORD_QUERY,
                variables,
                java.util.Arrays.asList("calendarSubmitRecord"),
                CalendarSubmitRecord.class
            );
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    public List<Cookie> getLeetcodeSession() {
        return httpClient.getCookies();
    }

    /**
     * 还是决定做个二级缓存, 因为Personal界面需要查询大量内容, 不做个缓存太耗时了
     */
    public LeetcodeUserProfile queryUserProfile() {
        if (leetcodeUserProfile != null) {
            return leetcodeUserProfile;
        }
        try {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            LeetcodeUserProfile result = graphqlClient.query(
                LeetcodeApiUtils.USER_PROFILE_PUBLIC_QUERY,
                variables,
                java.util.Arrays.asList("userProfilePublicProfile"),
                LeetcodeUserProfile.class
            );
            this.leetcodeUserProfile = result;
            return result;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 还是得做二级缓存, 查询用户做题进度
     */
    public UserQuestionProgress queryUserQuestionProgress() {
        if (userQuestionProgress != null) {
            return userQuestionProgress;
        }
        try {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            UserQuestionProgress result = graphqlClient.query(
                LeetcodeApiUtils.USER_QUESTION_PROGRESS_QUERY,
                variables,
                java.util.Arrays.asList("userQuestionProgress"),
                UserQuestionProgress.class
            );
            this.userQuestionProgress = result;
            return result;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 查询用户竞赛分数以及排名, 还是得做二级缓存
     */
    public UserContestRanking queryUserContestRanking() {
        if (userContestRanking != null) {
            return userContestRanking;
        }
        try {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            UserContestRanking result = graphqlClient.query(
                LeetcodeApiUtils.USER_CONTEST_RANKING_QUERY,
                variables,
                java.util.Arrays.asList("userContestRanking"),
                UserContestRanking.class
            );
            this.userContestRanking = result;
            return result;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 查询用户问题提交历史记录. 这个不做缓存, 因为每隔一段时间, 数据信息都会变化
     */
    public UserProgressQuestionList queryUserProgressQuestionList() {
        if (userProgressQuestionList != null) {
            return userProgressQuestionList;
        }
        try {
            UserProgressQuestionList result = graphqlClient.query(
                LeetcodeApiUtils.USER_PROGRESS_QUESTION_LIST_QUERY,
                new java.util.HashMap<>(),
                java.util.Arrays.asList("userProgressQuestionList"),
                UserProgressQuestionList.class
            );
            this.userProgressQuestionList = result;
            return result;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }
    public UserCalendar queryUserCalendar() {
        if (userCalendar != null) {
            return userCalendar;
        }
        String url = LeetcodeApiUtils.getLeetcodeReqNOJUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.USER_PROFILE_CALENDAR_QUERY);
        body.addVariable("userSlug", queryUserStatus().getUserSlug());
    
        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
            .setBody(body.toJsonStr())
            .setContentType("application/json")
            .addBasicHeader()
            .build();
    
        HttpResponse httpResponse = httpClient.executePost(httpRequest, project);
        String resp = httpResponse.getBody();
        JsonElement jsonElement = JsonParser.parseString(resp).getAsJsonObject().get("data").getAsJsonObject()
            .get("userCalendar");
        userCalendar = GsonUtils.fromJson(jsonElement, UserCalendar.class);
        return userCalendar;
    }

    @Subscribe
    public void subscribeCodeSubmitEvent(CodeSubmitEvent event) {
        // clear cache
        this.userProgressQuestionList = null;
        this.userQuestionProgress = null;
    }
}