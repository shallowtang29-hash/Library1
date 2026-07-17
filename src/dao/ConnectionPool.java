package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";
    private static final int POOL_SIZE = 10;

    private final BlockingQueue<Connection> pool;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private static volatile ConnectionPool instance;

    private ConnectionPool() {
        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            pool.offer(createConnection());
        }
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库连接失败！", e);
        }
    }

    public Connection getConnection() {
        if (closed.get()) {
            throw new RuntimeException("连接池已关闭！");
        }
        try {
            Connection conn = pool.poll(5, TimeUnit.SECONDS);
            if (conn == null) {
                throw new RuntimeException("获取数据库连接超时！");
            }
            if (conn.isClosed()) {
                conn = createConnection();
            }
            return new PooledConnection(conn, pool);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取数据库连接被中断！", e);
        } catch (SQLException e) {
            throw new RuntimeException("检测连接状态失败！", e);
        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            for (Connection conn : pool) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
