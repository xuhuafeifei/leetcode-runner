# 代码地图

> 关键词 → 文件/符号索引。按能力分节。

## 网络层

### GraphQL 接口客户端

- **关键词**：`graphql` `leetcode` `httpClient` `query` `mutation`
- **定位**：
  | 路径 | 符号 | 说明 |
  |------|------|------|
  | `io/http/LeetcodeClient.java` | `LeetcodeClient` | GraphQL 请求主入口，900 行，单例 |
  | `io/http/utils/HttpClient.java` | `HttpClient` | Apache HttpClient 封装，249 行 |
  | `io/http/utils/LeetcodeApiUtils.java` | `LeetcodeApiUtils` | API 工具类 |

### 本地 HTTP 服务

- **关键词**：`local server` `httpRequestHandler`
- **定位**：
  | 路径 | 符号 | 说明 |
  |------|------|------|
  | `io/http/LocalHttpRequestHandler.java` | `LocalHttpRequestHandler` | 本地请求处理 |
  | `io/http/LocalResourceHttpServer.java` | `LocalResourceHttpServer` | 本地资源服务 |

## 业务服务层

### 题目服务

- **关键词**：`question` `QuestionService`
- **定位**：
  | 路径 | 符号 |
  |------|------|
  | `service/QuestionService.java` | `QuestionService` |

### 提交服务

- **关键词**：`submission` `SubmissionService`
- **定位**：
  | 路径 | 符号 |
  |------|------|
  | `service/SubmissionService.java` | `SubmissionService` |
  | `editors/SubmissionEditor.java` | `SubmissionEditor` |
