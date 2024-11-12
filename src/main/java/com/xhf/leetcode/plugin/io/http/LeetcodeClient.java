package com.xhf.leetcode.plugin.io.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xhf.leetcode.plugin.io.file.StoreService;
import com.xhf.leetcode.plugin.io.http.utils.HttpClient;
import com.xhf.leetcode.plugin.io.http.utils.LeetcodeApiUtils;
import com.xhf.leetcode.plugin.model.*;
import com.xhf.leetcode.plugin.setting.AppSettings;
import com.xhf.leetcode.plugin.utils.GsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import java.util.ArrayList;
import java.util.List;

public class LeetcodeClient {

//    private static final LeetcodeClient lcClient = new LeetcodeClient();
//
//    public LeetcodeClient getInstance() {
//        return lcClient;
//    }

    private Project project;
    private final HttpClient httpClient;

    private LeetcodeClient(Project project) {
        this.project = project;
        // loadCache
        httpClient = HttpClient.getInstance();
        this.loadCache(project);
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


    private static volatile LeetcodeClient instance;

    public static LeetcodeClient getInstance(Project project) {
        if (instance != null) return instance;
        synchronized (LeetcodeClient.class) {
            if (instance == null) {
                instance = new LeetcodeClient(project);
            }
        }
        return instance;
    }

    /**
     * init instance
     * LeetcodeClient need to load cache, which may takes a lot of time. therefore,
     * it should be called in a thread when plugin is loaded
     * @param project
     */
    public static void init(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            getInstance(project);
        });
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

    /**
     * get all question need a lot of time
     * @return
     */
    public List<Question> getTotalQuestion() {
        // load cache
        List<Question> ans = loadQuestionCache();
        if (ans != null) {
            return ans;
        }

        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.PROBLEM_SET_QUERY);

        ans = new ArrayList<>();
        boolean flag = true;
        int skip = 0, limit = 100;
        while (flag) {
            SearchParams params = new SearchParams.ParamsBuilder()
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

        // store in first cache
        StoreService.getInstance(project).addCache(StoreService.QUESTION_LIST_KEY, ans, true);
        // store in second cache
        this.ql = ans;
        return ans;
    }

    // second cache
    private List<Question> ql;

    private List<Question> loadQuestionCache() {
        if (ql == null) {
            // if second cache is null, search the first cache
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
     * @return
     */
    public List<Question> getQuestionList(SearchParams params) {
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
     * fill question with code snippets, translated title and content
     * @param question
     */
    public void fillQuestion(Question question) {
        // get config
        String langType = AppSettings.getInstance().getLangType();

        String url = LeetcodeApiUtils.getLeetcodeReqUrl();
        // build graphql req
        GraphqlReqBody body = new GraphqlReqBody(LeetcodeApiUtils.QUESTION_CONTENT_QUERY);
        body.addVariable("titleSlug", question.getTitleSlug());

        HttpRequest httpRequest = new HttpRequest.RequestBuilder(url)
                .setBody(body.toJsonStr())
                .setContentType("application/json")
                .addBasicHeader()
                .build();

        HttpResponse httpResponse = httpClient.executePost(httpRequest);

        String resp = httpResponse.getBody();

        // parse json to array
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        JsonObject questionJsonObj = jsonObject.getAsJsonObject("data").getAsJsonObject("question");

        String translatedTitle = questionJsonObj.get("translatedTitle").getAsString();
        String translatedContent = questionJsonObj.get("translatedContent").getAsString().replaceAll("\n\n", "");
        String codeSnippets = null;

        for (JsonElement item : questionJsonObj.getAsJsonArray("codeSnippets")) {
            JsonObject obj = item.getAsJsonObject();
            if (GsonUtils.fromJson(obj.get("lang"), String.class).equals(langType)) {
                codeSnippets = obj.get("code").getAsString();
                break;
            }
        }

        // fill target obj
        question.setTranslatedTitle(translatedTitle);
        question.setTranslatedContent(translatedContent);
        question.setCodeSnippets(codeSnippets);
    }
}
