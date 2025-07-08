package com.xhf.leetcode.plugin.review.backend.database;

import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.review.backend.entity.Card;
import com.xhf.leetcode.plugin.utils.LogUtils;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author 文艺倾年
 */
public class Sqlite {

    private final String dbFolder;
    private final String dbName;
    private Connection connection;

    /**
     * Sqlite类的构造函数，用于设置数据库连接的相关信息
     */
    public Sqlite(String dbFolder, String dbName) {
        this.dbFolder = dbFolder;
        this.dbName = dbName;
    }

    /**
     * 连接到Sqlite数据库
     *
     * @return Sqlite对象
     */
    public Sqlite connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            // 保证兼容其他系统
            String dbUrl = "jdbc:sqlite:" + this.dbFolder + File.separator + this.dbName;
            LogUtils.info("[Database] 正在连接到数据库: " + dbUrl);
            // 初始化时自动创建目录结构
            initializeDatabaseDirectory();
            connection = DriverManager.getConnection(dbUrl);
            LogUtils.info("[Database] 成功连接到数据库");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(new JFrame(),
                "SQLLite错误: " + e,
                "建立数据库连接时发生错误", JOptionPane.ERROR_MESSAGE);
            LogUtils.info("[Database] 无法连接到数据库: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * 创建数据库目录结构（如果不存在）
     */
    private void initializeDatabaseDirectory() {
        // 创建目录（递归创建）
        File dir = new File(this.dbFolder);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建数据库目录: " +
                    dbFolder + " (权限不足或路径无效)");
            }
        }

        // 创建数据库文件（如果不存在）
        File dbFile = new File(dir, dbName);
        if (!dbFile.exists()) {
            try {
                boolean created = dbFile.createNewFile();
                if (!created) {
                    throw new RuntimeException("无法创建数据库文件: " +
                        dbName + " (可能已被其他进程占用)");
                }
                LogUtils.info("[Database] 自动创建新数据库文件: " + dbFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("创建数据库文件失败", e);
            }
        }
    }


    /**
     * 断开与Sqlite数据库的连接
     */
    public void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
    }

    /**
     * 使用PreparedStatement更新数据库
     *
     * @param statement PreparedStatement对象
     */
    public void update(PreparedStatement statement) {
        checkConnection();
        this.queryUpdate(statement);
    }

    /**
     * 使用字符串形式的Statement更新数据库
     *
     * @param statement SQL语句
     */
    public void update(String statement) {
        checkConnection();
        this.queryUpdate(statement);
    }

    /**
     * 同步更新数据库，使用字符串形式的Statement
     *
     * @param statement SQL语句
     */
    public void syncUpdate(String statement) {
        checkConnection();
        this.queryUpdate(statement);
    }

    /**
     * 执行数据库查询，使用PreparedStatement
     *
     * @param statement PreparedStatement对象
     * @param consumer 处理ResultSet的消费者
     */
    public void query(PreparedStatement statement, Consumer<ResultSet> consumer) {
        checkConnection();
        ResultSet result = this.query(statement);
        consumer.accept(result);
    }

    /**
     * 执行数据库查询，使用字符串形式的Statement
     *
     * @param statement SQL语句
     * @param consumer 处理ResultSet的消费者
     */
    public void query(String statement, Consumer<ResultSet> consumer) {
        checkConnection();
        ResultSet result = this.query(statement);
        consumer.accept(result);
    }

    /**
     * 使用字符串形式的查询执行查询
     *
     * @param query 查询语句
     * @return ResultSet对象
     */
    public ResultSet query(String query) {
        checkConnection();
        try {
            return query(this.connection.prepareStatement(query));
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 使用PreparedStatement执行查询
     *
     * @param statement PreparedStatement对象
     * @return ResultSet对象
     */
    public ResultSet query(PreparedStatement statement) {
        checkConnection();
        try {
            return statement.executeQuery();
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 同步执行数据库查询，使用字符串形式的Statement
     *
     * @param statement SQL语句
     * @param consumer 处理ResultSet的消费者
     */
    public void syncQuery(String statement, Consumer<ResultSet> consumer) {
        ResultSet result = this.query(statement);
        consumer.accept(result);
    }

    /**
     * 准备PreparedStatement
     *
     * @param query 查询语句
     * @return PreparedStatement对象
     */
    public PreparedStatement prepare(String query) {
        checkConnection();
        try {
            return this.connection.prepareStatement(query);
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 使用字符串形式的查询更新数据库
     *
     * @param query 查询语句
     */
    public void queryUpdate(String query) {
        checkConnection();
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            queryUpdate(statement);
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
    }

    /**
     * 使用PreparedStatement更新数据库
     *
     * @param preparedStatement PreparedStatement对象
     */
    public void queryUpdate(PreparedStatement preparedStatement) {
        checkConnection();
        try {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        } finally {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            }
        }
    }

    /**
     * 检查数据库连接是否有效
     */
    private void checkConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                connect();
            }
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
        }
    }

    /**
     * 返回数据库连接是否有效
     *
     * @return 连接状态布尔值
     */
    public boolean isConnected() {
        return connection != null;
    }

    /**
     * 开始事务
     */
    public void beginTransaction() {
        checkConnection();
        try {
            connection.setAutoCommit(false);
            LogUtils.info("[Database] 事务开始");
        } catch (SQLException e) {
            LogUtils.warn("[Database] 开始事务失败: " + e.getMessage());
            throw new RuntimeException("开始事务失败", e);
        }
    }

    /**
     * 提交事务
     */
    public void commitTransaction() {
        checkConnection();
        try {
            connection.commit();
            connection.setAutoCommit(true);
            LogUtils.info("[Database] 事务提交成功");
        } catch (SQLException e) {
            LogUtils.warn("[Database] 提交事务失败: " + e.getMessage());
            throw new RuntimeException("提交事务失败", e);
        }
    }

    /**
     * 回滚事务
     */
    public void rollbackTransaction() {
        checkConnection();
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            LogUtils.info("[Database] 事务回滚成功");
        } catch (SQLException e) {
            LogUtils.warn("[Database] 回滚事务失败: " + e.getMessage());
            throw new RuntimeException("回滚事务失败", e);
        }
    }


    /**
     * 执行查询并返回List<Map>结果
     *
     * @param query SQL查询语句
     * @return 包含查询结果的List<Map>
     */
    public List<Map<String, Object>> queryForList(String query) {
        checkConnection();
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            LogUtils.warn("[Database] 查询失败: " + e.getMessage());
            throw new RuntimeException("查询失败", e);
        }
        return resultList;
    }

    /**
     * 执行查询并返回List<Card>结果
     *
     * @param query SQL查询语句
     * @return 包含查询结果的List<Card>
     */
    public List<Card> queryForCards(String query) {
        checkConnection();
        List<Card> resultList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Card card = new Card();
                card.setCardId(resultSet.getInt("card_id"));
                card.setFront(resultSet.getString("front"));
                card.setBack(resultSet.getString("back"));
                card.setCreated(resultSet.getLong("created"));
                card.setStability(resultSet.getFloat("stability"));
                card.setDifficulty(resultSet.getFloat("difficulty"));
                card.setElapsedDays(resultSet.getInt("elapsed_days"));
                card.setRepetitions(resultSet.getInt("repetitions"));
                card.setDayInterval(resultSet.getInt("day_interval"));
                card.setState(resultSet.getInt("state"));
                card.setNextRepetition(resultSet.getLong("next_repetition"));
                card.setLastReview(resultSet.getLong("last_review"));
                resultList.add(card);
            }
        } catch (SQLException e) {
            LogUtils.warn("[Database] 查询失败: " + e.getMessage());
            throw new RuntimeException("查询失败", e);
        }
        return resultList;
    }

    /**
     * 向表中插入数据
     *
     * @param tableName 表名
     * @param data 包含字段名和值的Map
     */
    public void insert(String tableName, Map<String, Object> data) {
        checkConnection();
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("插入数据不能为空");
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            params.add(entry.getValue());
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
            tableName, columns, values);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            statement.executeUpdate();
            LogUtils.info("[Database] 成功插入数据到表: " + tableName);
        } catch (SQLException e) {
            LogUtils.warn("[Database] 插入数据失败: " + e.getMessage());
            throw new RuntimeException("插入数据失败", e);
        }
    }
}
