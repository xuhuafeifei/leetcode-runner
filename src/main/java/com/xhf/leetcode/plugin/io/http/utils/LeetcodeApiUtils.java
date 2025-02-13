package com.xhf.leetcode.plugin.io.http.utils;

/**
 * leetcode api request utils
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class LeetcodeApiUtils {
    public static final String leetcode = "leetcode.com";
    public static final String leetcodecn = "leetcode.cn";
    public static final String LEETCODE_SESSION = "LEETCODE_SESSION";
    public static final String ARTICLE_CONTENT_QUERY = "query qaQuestionDetail($uuid: ID!) {\n  qaQuestion(uuid: $uuid) {\n    ...qaQuestion\n    myAnswerId\n    __typename\n  }\n}\n\nfragment qaQuestion on QAQuestionNode {\n  ipRegion\n  uuid\n  slug\n  title\n  thumbnail\n  summary\n  content\n  slateValue\n  sunk\n  pinned\n  pinnedGlobally\n  byLeetcode\n  isRecommended\n  isRecommendedGlobally\n  subscribed\n  hitCount\n  numAnswers\n  numPeopleInvolved\n  numSubscribed\n  createdAt\n  updatedAt\n  status\n  identifier\n  resourceType\n  articleType\n  alwaysShow\n  alwaysExpand\n  score\n  favoriteCount\n  isMyFavorite\n  isAnonymous\n  canEdit\n  reactionType\n  atQuestionTitleSlug\n  blockComments\n  reactionsV2 {\n    count\n    reactionType\n    __typename\n  }\n  tags {\n    name\n    nameTranslated\n    slug\n    imgUrl\n    tagType\n    __typename\n  }\n  subject {\n    slug\n    title\n    __typename\n  }\n  contentAuthor {\n    ...contentAuthor\n    __typename\n  }\n  realAuthor {\n    ...realAuthor\n    __typename\n  }\n  __typename\n}\n\nfragment contentAuthor on ArticleAuthor {\n  username\n  userSlug\n  realName\n  avatar\n  __typename\n}\n\nfragment realAuthor on UserNode {\n  username\n  profile {\n    userSlug\n    realName\n    userAvatar\n    __typename\n  }\n  __typename\n}\n";

    private static final String leetcodeUrl = "https://";
    private static String leetcodeGraphql = "/graphql";
    private static String leetcodeLogin = "/accounts/login/";

    public static final String PROBLEM_SET_QUERY = "\n    query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {\n  problemsetQuestionList(\n    categorySlug: $categorySlug\n    limit: $limit\n    skip: $skip\n    filters: $filters\n  ) {\n    hasMore\n    total\n    questions {\n      acRate\n      difficulty\n      freqBar\n      frontendQuestionId\n      isFavor\n      paidOnly\n      solutionNum\n      status\n      title\n      titleCn\n      titleSlug\n      topicTags {\n        name\n        nameTranslated\n        id\n        slug\n      }\n      extra {\n        hasVideoSolution\n        topCompanyTags {\n          imgUrl\n          slug\n          numSubscribed\n        }\n      }\n    }\n  }\n}\n    ";
    public static final String QUESTION_DATA_QUERY = "query questionData($titleSlug: String!) {\n  question(titleSlug: $titleSlug) {\n    questionId\n    questionFrontendId\n    categoryTitle\n    boundTopicId\n    title\n    titleSlug\n    content\n    translatedTitle\n    translatedContent\n    isPaidOnly\n    difficulty\n    likes\n    dislikes\n    isLiked\n    similarQuestions\n    contributors {\n      username\n      profileUrl\n      avatarUrl\n      __typename\n    }\n    langToValidPlayground\n    topicTags {\n      name\n      slug\n      translatedName\n      __typename\n    }\n    companyTagStats\n    codeSnippets {\n      lang\n      langSlug\n      code\n      __typename\n    }\n    stats\n    hints\n    solution {\n      id\n      canSeeDetail\n      __typename\n    }\n    status\n    sampleTestCase\n    metaData\n    judgerAvailable\n    judgeType\n    mysqlSchemas\n    enableRunCode\n    envInfo\n    book {\n      id\n      bookName\n      pressName\n      source\n      shortDescription\n      fullDescription\n      bookImgUrl\n      pressImgUrl\n      productUrl\n      __typename\n    }\n    isSubscribed\n    isDailyQuestion\n    dailyRecordStatus\n    editorType\n    ugcQuestionId\n    style\n    exampleTestcases\n    __typename\n  }\n}\n";
    public static final String QUESTION_STATUS_QUERY = "query allQuestionsStatuses {\n  allQuestionsBeta {\n    ...questionStatusFields\n    __typename\n  }\n}\n\nfragment questionStatusFields on QuestionNode {\n  questionId\n  status\n  __typename\n}\n";
    public static final String QUESTION_CONTENT_QUERY = "query questionData($titleSlug: String!) {\n  question(titleSlug: $titleSlug) {\n    questionId\n    questionFrontendId\n    categoryTitle\n    boundTopicId\n    title\n    titleSlug\n    content\n    translatedTitle\n    translatedContent\n    isPaidOnly\n    difficulty\n    likes\n    dislikes\n    isLiked\n    similarQuestions\n    contributors {\n      username\n      profileUrl\n      avatarUrl\n      __typename\n    }\n    langToValidPlayground\n    topicTags {\n      name\n      slug\n      translatedName\n      __typename\n    }\n    companyTagStats\n    codeSnippets {\n      lang\n      langSlug\n      code\n      __typename\n    }\n    stats\n    hints\n    solution {\n      id\n      canSeeDetail\n      __typename\n    }\n    status\n    sampleTestCase\n    metaData\n    judgerAvailable\n    judgeType\n    mysqlSchemas\n    enableRunCode\n    envInfo\n    book {\n      id\n      bookName\n      pressName\n      source\n      shortDescription\n      fullDescription\n      bookImgUrl\n      pressImgUrl\n      productUrl\n      __typename\n    }\n    isSubscribed\n    isDailyQuestion\n    dailyRecordStatus\n    editorType\n    ugcQuestionId\n    style\n    exampleTestcases\n    __typename\n  }\n}\n";

    public static final String QUESTION_OF_TODAY_QUERY = "\n    query questionOfToday {\n  todayRecord {\n    date\n    userStatus\n    question {\n      questionId\n      frontendQuestionId: questionFrontendId\n      difficulty\n      title\n      titleCn: translatedTitle\n      titleSlug\n      paidOnly: isPaidOnly\n      freqBar\n      isFavor\n      acRate\n      status\n      solutionNum\n      hasVideoSolution\n      topicTags {\n        name\n        nameTranslated: translatedName\n        id\n      }\n      extra {\n        topCompanyTags {\n          imgUrl\n          slug\n          numSubscribed\n        }\n      }\n    }\n    lastSubmission {\n      id\n    }\n  }\n}\n    ";

    public static final String USER_STATUS_QUERY = "\n    query globalData {\n  userStatus {\n    isSignedIn\n    isPremium\n    username\n    realName\n    avatar\n    userSlug\n    isAdmin\n    useTranslation\n    premiumExpiredAt\n    isTranslator\n    isSuperuser\n    isPhoneVerified\n    isVerified\n  }\n  jobsMyCompany {\n    nameSlug\n  }\n  commonNojPermissionTypes\n}\n    ";

    public static final String SOLUTION_LIST_QUERY = "\n    query questionTopicsList($questionSlug: String!, $skip: Int, $first: Int, $orderBy: SolutionArticleOrderBy, $userInput: String, $tagSlugs: [String!]) {\n  questionSolutionArticles(\n    questionSlug: $questionSlug\n    skip: $skip\n    first: $first\n    orderBy: $orderBy\n    userInput: $userInput\n    tagSlugs: $tagSlugs\n  ) {\n    totalNum\n    edges {\n      node {\n        rewardEnabled\n        canEditReward\n        uuid\n        title\n        slug\n        sunk\n        chargeType\n        status\n        identifier\n        canEdit\n        canSee\n        reactionType\n        hasVideo\n        favoriteCount\n        upvoteCount\n        reactionsV2 {\n          count\n          reactionType\n        }\n        tags {\n          name\n          nameTranslated\n          slug\n          tagType\n        }\n        createdAt\n        thumbnail\n        author {\n          username\n          profile {\n            userAvatar\n            userSlug\n            realName\n            reputation\n          }\n        }\n        summary\n        topic {\n          id\n          commentCount\n          viewCount\n          pinned\n        }\n        byLeetcode\n        isMyFavorite\n        isMostPopular\n        isEditorsPick\n        hitCount\n        videosInfo {\n          videoId\n          coverUrl\n          duration\n        }\n      }\n    }\n  }\n}\n    ";
    public static final String SOLUTION_CONTENT_QUERY = "\n    query discussTopic($slug: String) {\n  solutionArticle(slug: $slug, orderBy: DEFAULT) {\n    ...solutionArticle\n    content\n    next {\n      slug\n      title\n    }\n    prev {\n      slug\n      title\n    }\n  }\n}\n    \n    fragment solutionArticle on SolutionArticleNode {\n  ipRegion\n  rewardEnabled\n  canEditReward\n  uuid\n  title\n  content\n  slateValue\n  slug\n  sunk\n  chargeType\n  status\n  identifier\n  canEdit\n  canSee\n  reactionType\n  reactionsV2 {\n    count\n    reactionType\n  }\n  tags {\n    name\n    nameTranslated\n    slug\n    tagType\n  }\n  createdAt\n  thumbnail\n  author {\n    username\n    isDiscussAdmin\n    isDiscussStaff\n    profile {\n      userAvatar\n      userSlug\n      realName\n      reputation\n    }\n  }\n  summary\n  topic {\n    id\n    subscribed\n    commentCount\n    viewCount\n    post {\n      id\n      status\n      voteStatus\n      isOwnPost\n    }\n  }\n  byLeetcode\n  isMyFavorite\n  isMostPopular\n  favoriteCount\n  isEditorsPick\n  hitCount\n  videosInfo {\n    videoId\n    coverUrl\n    duration\n  }\n  question {\n    titleSlug\n    questionFrontendId\n  }\n}\n    ";

    public static final String SUBMISSION_LIST_QUERY = "\n    query submissionList($offset: Int!, $limit: Int!, $lastKey: String, $questionSlug: String!, $lang: String, $status: SubmissionStatusEnum) {\n  submissionList(\n    offset: $offset\n    limit: $limit\n    lastKey: $lastKey\n    questionSlug: $questionSlug\n    lang: $lang\n    status: $status\n  ) {\n    lastKey\n    hasNext\n    submissions {\n      id\n      title\n      status\n      statusDisplay\n      lang\n      langName: langVerboseName\n      runtime\n      timestamp\n      url\n      isPending\n      memory\n      submissionComment {\n        comment\n        flagType\n      }\n    }\n  }\n}\n    ";
    public static final String SUBMISSION_CONTENT_QUERY = "\n    query submissionDetails($submissionId: ID!) {\n  submissionDetail(submissionId: $submissionId) {\n    code\n    timestamp\n    statusDisplay\n    isMine\n    runtimeDisplay: runtime\n    memoryDisplay: memory\n    memory: rawMemory\n    lang\n    langVerboseName\n    question {\n      questionId\n      titleSlug\n      hasFrontendPreview\n    }\n    user {\n      realName\n      userAvatar\n      userSlug\n    }\n    runtimePercentile\n    memoryPercentile\n    submissionComment {\n      flagType\n    }\n    passedTestCaseCnt\n    totalTestCaseCnt\n    fullCodeOutput\n    testDescriptions\n    testInfo\n    testBodies\n    stdOutput\n    ... on GeneralSubmissionNode {\n      outputDetail {\n        codeOutput\n        expectedOutput\n        input\n        compileError\n        runtimeError\n        lastTestcase\n      }\n    }\n    ... on ContestSubmissionNode {\n      outputDetail {\n        codeOutput\n        expectedOutput\n        input\n        compileError\n        runtimeError\n        lastTestcase\n      }\n    }\n  }\n}\n    ";

    public static String getLeetcodeHost() {
        return leetcodecn;
    }

    public static String getLeetcodeUrl() {
        return leetcodeUrl + getLeetcodeHost();
    }

    public static String getQuestionUrl(String titleSlug) {
        return getLeetcodeUrl() + "/problems/" + titleSlug + "/description";
    }

    public static String getSolutionUrl(String titleSlug, String topicId, String solutionSlug) {
        return getLeetcodeUrl() + "/problems/" + titleSlug + "/solutions/" + topicId + "/" + solutionSlug;
    }

    public static String getRunCodeUrl(String titleSlug) {
        return getLeetcodeUrl() + "/problems/" + titleSlug + "/interpret_solution/";
    }

    public static String getSubmitCodeUrl(String titleSlug) {
        return getLeetcodeUrl() + "/problems/" + titleSlug + "/submit/";
    }

    public static String getSubmissionCheckUrl(String id) {
        return getLeetcodeUrl() + "/submissions/detail/" + id + "/check/";
    }

    public static String getLeetcodeReqUrl() {
        return getLeetcodeUrl() + leetcodeGraphql;
    }

    public static String getLeetcodeLogin() {
        return getLeetcodeUrl() + leetcodeLogin;
    }

}
