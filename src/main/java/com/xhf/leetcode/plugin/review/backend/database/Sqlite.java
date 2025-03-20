package com.xhf.leetcode.plugin.review.backend.database;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.function.Consumer;

/**
 * @author 文艺倾年
 */
public class Sqlite {
    private String dbFolder, dbName;
    private ExecutorService executorService;
    private Connection connection;

    /**
     * Sqlite类的构造函数，用于设置数据库连接的相关信息
     *
     */
    public Sqlite(String dbFolder, String dbName) {
        this.dbFolder = dbFolder;
        this.dbName = dbName;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 连接到Sqlite数据库
     * @return Sqlite对象
     */
    public Sqlite connect() {
        try {
            // 保证兼容其他系统
            String dbUrl = "jdbc:sqlite:" + this.dbFolder + File.separator + this.dbName;
            // 初始化时自动创建目录结构
            initializeDatabaseDirectory();
            connection = DriverManager.getConnection(dbUrl);
            System.out.println("[Database] 成功连接到数据库");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(),
                    "SQLLite错误: " + e,
                    "建立数据库连接时发生错误", JOptionPane.ERROR_MESSAGE);
            System.out.println("[Database] 无法连接到数据库: " + e);
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
                System.out.println("[Database] 自动创建新数据库文件: " + dbFile.getAbsolutePath());
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
            e.printStackTrace();
        }
    }

    /**
     * 使用PreparedStatement更新数据库
     * @param statement PreparedStatement对象
     */
    public void update(PreparedStatement statement) {
        checkConnection();
        this.executorService.execute(() -> this.queryUpdate(statement));
    }

    /**
     * 使用字符串形式的Statement更新数据库
     * @param statement SQL语句
     */
    public void update(String statement) {
        checkConnection();
        this.executorService.execute(() -> this.queryUpdate(statement));
    }

    /**
     * 同步更新数据库，使用字符串形式的Statement
     * @param statement SQL语句
     */
    public void syncUpdate(String statement) {
        checkConnection();
        this.queryUpdate(statement);
    }

    /**
     * 执行数据库查询，使用PreparedStatement
     * @param statement PreparedStatement对象
     * @param consumer  处理ResultSet的消费者
     */
    public void query(PreparedStatement statement, Consumer<ResultSet> consumer) {
        checkConnection();
        this.executorService.execute(() -> {
            ResultSet result = this.query(statement);
            consumer.accept(result);
        });
    }

    /**
     * 执行数据库查询，使用字符串形式的Statement
     * @param statement SQL语句
     * @param consumer  处理ResultSet的消费者
     */
    public void query(String statement, Consumer<ResultSet> consumer) {
        checkConnection();
        this.executorService.execute(() -> {
            ResultSet result = this.query(statement);
            consumer.accept(result);
        });
    }

    /**
     * 使用字符串形式的查询执行查询
     * @param query 查询语句
     * @return ResultSet对象
     */
    public ResultSet query(String query) {
        checkConnection();
        try {
            return query(this.connection.prepareStatement(query));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用PreparedStatement执行查询
     * @param statement PreparedStatement对象
     * @return ResultSet对象
     */
    public ResultSet query(PreparedStatement statement) {
        checkConnection();
        try {
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 同步执行数据库查询，使用字符串形式的Statement
     * @param statement SQL语句
     * @param consumer  处理ResultSet的消费者
     */
    public void syncQuery(String statement, Consumer<ResultSet> consumer) {
        ResultSet result = this.query(statement);
        consumer.accept(result);
    }

    /**
     * 准备PreparedStatement
     * @param query 查询语句
     * @return PreparedStatement对象
     */
    public PreparedStatement prepare(String query) {
        checkConnection();
        try {
            return this.connection.prepareStatement(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用字符串形式的查询更新数据库
     * @param query 查询语句
     */
    public void queryUpdate(String query) {
        checkConnection();
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            queryUpdate(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用PreparedStatement更新数据库
     * @param preparedStatement PreparedStatement对象
     */
    public void queryUpdate(PreparedStatement preparedStatement) {
        checkConnection();
        try {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查数据库连接是否有效
     */
    private void checkConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回数据库连接是否有效
     * @return 连接状态布尔值
     */
    public boolean isConnected() {
        return connection != null;
    }
}
