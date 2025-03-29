package com.xhf.leetcode.plugin.review.backend.database;

import com.intellij.openapi.application.PathManager;

/**
 * @author 文艺倾年
 */
public class DatabaseAdapter {
    private Sqlite sqlite;
    private String dbFolder, dbName;

    /**
     * 数据库适配器的构造函数，从配置文件中读取所有数据并建立数据库连接。
     * 此外，还会创建所有必要的表。
     */
    public DatabaseAdapter() {
        this.dbFolder  = "E:/data";
//        this.dbFolder  = PathManager.getSystemPath();
        this.dbName = "memory.db";

        // 使用加载的数据创建MySQL对象
        this.sqlite = new Sqlite(this.dbFolder, this.dbName).connect();

        // 创建数据库表
        this.createTables();
    }

    /**
     * 数据库：Sqlite
     * 如果表不存在，则在数据库中创建应用程序所需的所有表。
     * 创建卡片表：
     * card_id: 类型为 int，卡片ID，主键
     * front: 类型为 TEXT，卡片正面内容，不能为空
     * back: 类型为 TEXT，卡片背面内容，写自己的题解，可以为空
     * created: 类型为 BIGINT，卡片的创建时间戳（以毫秒为单位），不能为空
     * stability: 类型为 FLOAT，表示卡片的记忆稳定性，默认值为 0。
     * difficulty: 类型为 FLOAT，表示卡片的难度，默认值为 0。
     * elapsed_days: 类型为 INT，表示从上次复习到现在的天数，默认值为 0。
     * repetitions: 类型为 INT，表示卡片的重复次数，默认值为 0。
     * state: 类型为 INT，表示卡片的状态，默认值为 'NEW'：0，可能的状态包括 'NEW'：0, 'LEARNING'：1, 'REVIEWING'：2 等。
     * day_interval: 类型为 INT，表示两次复习之间的间隔天数，默认值为 0。
     * next_repetition: 类型为 BIGINT，表示下一次复习的时间戳（以毫秒为单位），默认值为 0。
     * last_review: 类型为 BIGINT，表示上一次复习的时间戳（以毫秒为单位），默认值为 0。
     * CREATE TABLE IF NOT EXISTS cards (
     * card_id int NOT NULL,
     * front VARCHAR(100),
     * back TEXT,
     * stability FLOAT DEFAULT 0,
     * difficulty FLOAT DEFAULT 0,
     * elapsed_days INT DEFAULT 0,
     * repetitions INT DEFAULT 0,
     * state INT DEFAULT 0,
     * next_repetition BIGINT DEFAULT 0,
     * last_review BIGINT DEFAULT 0,
     * PRIMARY KEY (card_id)
     * )
     * 未来考虑实现显示遗忘曲线。
     */
    private void createTables() {
        // 重新生成对应的语句
        this.sqlite.queryUpdate("CREATE TABLE IF NOT EXISTS cards (card_id int NOT NULL, front TEXT, back TEXT, created BIGINT NOT NULL, stability FLOAT DEFAULT 0, difficulty FLOAT DEFAULT 0, elapsed_days INT DEFAULT 0, repetitions INT DEFAULT 0, day_interval INT DEFAULT 0, state INT DEFAULT 0, next_repetition BIGINT DEFAULT 0, last_review BIGINT DEFAULT 0, PRIMARY KEY (card_id))");
   }

    /**
     * 获取用于建立数据库连接的Sqlite对象
     * @return Sqlite对象
     */
    public Sqlite getSqlite() {
        return this.sqlite;
    }
}
