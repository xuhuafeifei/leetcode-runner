# 001 — GraphQL 客户端选型

- **状态**：accepted
- **上下文**：当前 LeetcodeClient 基于 Apache HttpClient 4.x 手写 GraphQL 请求，代码重复多、无重试、超时严重。需要重构底层网络层。

## 备选方案

### 方案 A：OkHttp + 轻量封装（推荐）
- 用 OkHttp 替换 Apache HttpClient 4.x
- 自建 `GraphqlClient` 工具类封装 query/mutation
- 内置重试、超时、错误分类
- 优点：改动小、依赖轻（~500KB）、灵活
- 缺点：没有类型安全，query 还是字符串

### 方案 B：Apollo Kotlin
- 引入 Apollo Kotlin 客户端 + Gradle 插件
- 从 LeetCode introspection 拉 schema，自动生成代码
- 优点：类型安全、内置缓存、功能强大
- 缺点：配置复杂、schema 维护成本高、包体积大、学习曲线陡

### 方案 C：保持现状 + 加重试
- 继续用 Apache HttpClient
- 加个重试工具类
- 优点：改动最小
- 缺点：代码依旧啰嗦，维护成本高

## 决策
选 **方案 A**。

理由：
1. LeetCode GraphQL schema 不公开且频繁变动，类型安全收益不大
2. OkHttp 轻量可靠，IDE 插件环境成熟
3. 自制 GraphqlClient 能满足需求，且可逐步演进
4. 改动范围可控，风险低

## 后果
- 需要引入 Kotlin 协程（项目已有 Kotlin 插件，基础 OK）
- 网络层代码从 Java 迁移到 Kotlin
- 业务层接口保持兼容