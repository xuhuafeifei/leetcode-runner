package com.xhf.leetcode.plugin.io.http;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.bus.ClearCacheEvent;
import com.xhf.leetcode.plugin.bus.LCEventBus;
import com.xhf.leetcode.plugin.bus.LCSubscriber;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import com.xhf.leetcode.plugin.utils.RandomUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
// 订阅clearCache事件, 当该事件触发后, 清除cookie和ql(List<Question>, 是题目数据的二级缓存)
@LCSubscriber(events = {ClearCacheEvent.class})
public class LeetcodeClient {

    private Project project;
    private final HttpClient httpClient;
    private static boolean first = true;
    private static volatile LeetcodeClient instance;


    private LeetcodeClient(Project project) {
        this.project = project;
        // loadCache
        httpClient = HttpClient.getInstance();
        this.loadCache(project);
        LCEventBus.getInstance().register(this);
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


    public static LeetcodeClient getInstance(Project project) {
        if (instance != null) return instance;
        synchronized (LeetcodeClient.class) {
            if (instance == null) {
                instance = new LeetcodeClient(project);
            }
        }
        return instance;
    }


    // this method used for test
    @Deprecated
    private LeetcodeClient() {
        httpClient = HttpClient.getInstance();
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
     * @param project
     */
    public static void init(Project project) {
        if (first) {
            first = false;
            ApplicationManager.getApplication().invokeLater(() -> {
                getInstance(project);
            });
        }
    }


    private void setCookie(Cookie cookie, boolean needCache) {
        if (cookie.getName().equals(LeetcodeApiUtils.LEETCODE_SESSION)) {
            // need to store cache
            if (needCache) {
                StoreService.getInstance(project).addCache(StoreService.LEETCODE_SESSION_KEY, cookie.getValue());
            }
        }
        httpClient.setCookie(cookie);
    }

    /**
     * set and persist cookie if cookie name equals to LEETCODE_SESSION, which represent the user info
     * @param cookie
     */
    public void setCookie(Cookie cookie) {
        setCookie(cookie, true);
    }

    public void setCookies(List<Cookie> cookieList) {
        for (Cookie cookie : cookieList) {
            setCookie(cookie);
        }
    }

    public boolean isLogin() {
        // check LEETCODE_SESSION
        if (! httpClient.containsCookie(LeetcodeApiUtils.LEETCODE_SESSION)) {
            return Boolean.FALSE;
        }

        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.USER_STATUS_QUERY);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        // extract field
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        JsonObject userStatusObject = dataObject.getAsJsonObject("userStatus");

        // extract isSignedIn
        return userStatusObject.get("isSignedIn").getAsBoolean();
    }

    @Deprecated // leetcode login api is not suitable
    public Boolean login(String username, String password) {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .addJsonBody("username", username)
                .addJsonBody("password", password)
                .build();
        HttpResponse httpResponse = httpClient.executePost(httpRequest);
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
                }else if (status.equals("NOT_STARTED")) {
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

    public List<Question> queryTotalQuestion() {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.PROBLEM_SET_QUERY);

        List<Question> ans = new ArrayList<>();
        boolean flag = true;
        int skip = 0, limit = 100;
        while (flag) {
            GraphqlReqBody.SearchParams params = new GraphqlReqBody.SearchParams.ParamsBuilder()
                    .setCategorySlug("all-code-essentials")
                    .setLimit(limit)
                    .setSkip(skip)
                    .build();
            body.setBySearchParams(params);

            HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                    .setBody(body.toJsonStr())
                    .setContentType("application/json")
                    .addBasicHeader()
                    .build();

            HttpResponse httpResponse = httpClient.executePost(httpRequest);
            String resp = httpResponse.getBody();

            // parse json to array
            JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
            JsonObject pql = jsonObject.getAsJsonObject("data").getAsJsonObject("problemsetQuestionList");

            // no questions left
            if (! pql.get("hasMore").getAsBoolean()) {
                flag = false;
            }
            // parse json array
            JsonArray jsonArray = pql.getAsJsonArray("questions");
            List<Question> questions = GsonUtils.fromJsonArray(jsonArray, Question.class);
            // merge
            ans.addAll(questions);

            body.clear();
            skip = skip + limit;
        }

        return ans;
    }

    // second cache
    private List<Question> ql;

    private List<Question> loadQuestionCache() {
        if (ql == null) {
            // if the second cache is null, search the first cache
            String questionListJson = StoreService.getInstance(project).getCacheJson(StoreService.QUESTION_LIST_KEY);
            if (StringUtils.isBlank(questionListJson)) {
                return null;
            }
            ql = GsonUtils.fromJsonToList(questionListJson, Question.class);
            return ql;
        }else {
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

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

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
     *
     */
    public String queryQuestionInfoJson(GraphqlReqBody.SearchParams params) {
        if (StringUtils.isBlank(params.getTitleSlug())) {
            throw new RuntimeException("title slug is null ! " + GsonUtils.toJsonStr(params));
        }

        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_CONTENT_QUERY);
        body.setBySearchParams(params);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        return httpClient.executePost(httpRequest).getBody();
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

    /**
     * get today question
     */
    public Question getTodayQuestion(Project project) {
        StoreService service = StoreService.getInstance(project);
        Question q = service.getCache(StoreService.LEETCODE_TODAY_QUESTION_KEY, Question.class);
        if (q != null) {
            return q;
        }
        q = new Question();

        /* search question */
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_OF_TODAY_QUERY);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonElement jsonElement = jsonObject.getAsJsonObject("data").getAsJsonArray("todayRecord").get(0);
        JsonObject questionJOB = jsonElement.getAsJsonObject().getAsJsonObject("question");

        // extract field
        String frontendQuestionId = questionJOB.get("frontendQuestionId").getAsString();
        String difficulty = questionJOB.get("difficulty").getAsString();
        String title = questionJOB.get("title").getAsString();
        String titleSlug = questionJOB.get("titleSlug").getAsString();
        String titleCn = questionJOB.get("titleCn").getAsString();

        q.setFrontendQuestionId(frontendQuestionId);
        q.setDifficulty(difficulty);
        q.setTitle(title);
        q.setTitleSlug(titleSlug);
        q.setTitleCn(titleCn);

        /* store question */
        service.addCache(StoreService.LEETCODE_TODAY_QUESTION_KEY, q, false);

        return q;
    }

    /**
     * run code by leetcode platform
     *
     */
    public RunCodeResult runCode(RunCode runCodeModel) {
        /* check params */
        if (StringUtils.isBlank(runCodeModel.getQuestionId())||
                StringUtils.isBlank(runCodeModel.getLang())||
                StringUtils.isBlank(runCodeModel.getDataInput())||
                StringUtils.isBlank(runCodeModel.getTypeCode())||
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

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

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
        if (StringUtils.isBlank(runCodeModel.getQuestionId())||
                StringUtils.isBlank(runCodeModel.getLang())||
                StringUtils.isBlank(runCodeModel.getTypeCode())||
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

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

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

        HttpResponse httpResponse = httpClient.executeGet(httpRequest);

        // check data
        while (! checkLeetcodeReady(httpResponse)) {
            httpResponse = httpClient.executeGet(httpRequest);
        }

        return httpResponse.getBody();
    }

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
        return ! state.getAsString().equals("STARTED");
    }

    public List<Solution> querySolutionList(String questionSlug) {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.SOLUTION_LIST_QUERY);
        // build by params
        body.setBySearchParams(new GraphqlReqBody.SearchParams.ParamsBuilder()
                .setQuestionSlug(questionSlug)
                .setSkip(0)
                .setFirst(50)
                .setOrderBy("DEFAULT")
                .build()
        );

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonObject("data")
                .getAsJsonObject("questionSolutionArticles")
                .getAsJsonArray("edges");

        List<Solution> res = new ArrayList<>(15);
        for (JsonElement element : jsonArray) {
            JsonElement node = element.getAsJsonObject().get("node");
            Solution solution = GsonUtils.fromJson(node, Solution.class);
            res.add(solution);
        }

        return res;
    }

    public String getSolutionContent(String solutionSlug) {
         String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.SOLUTION_CONTENT_QUERY);
        body.addVariable("slug", solutionSlug);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();

        return jsonObject.getAsJsonObject("data")
                .getAsJsonObject("solutionArticle")
                .get("content")
                .getAsString();
    }

    public List<Submission> getSubmissionList(String slug) {
          String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.SUBMISSION_LIST_QUERY);
        body.addVariable("questionSlug", slug);
        body.addVariable("offset", 0);
        body.addVariable("limit", 50);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();

        JsonArray jsonArray = jsonObject.getAsJsonObject("data")
                .getAsJsonObject("submissionList")
                .getAsJsonArray("submissions");

        return GsonUtils.fromJsonArray(jsonArray, Submission.class);
    }

    public String getSubmissionCode(String submissionId) {
        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.SUBMISSION_CONTENT_QUERY);
        body.addVariable("submissionId", submissionId);

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();

        return jsonObject.getAsJsonObject("data").getAsJsonObject("submissionDetail").get("code").getAsString();
    }

    public void cacheQuestionList(List<Question> totalQuestion) {
        ql = totalQuestion;
        asyncPersistQuestionCache();
    }
}