package dao;

import model.User;
import exception.UserNotFoundException;
import exception.DataAccessException;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachingUserDao implements UserDao {

    private final UserDao sqlDao;
    private final ArrayListUserDao memoryCache;
    // volatile关键字确保memoryLoaded变量在多线程间的可见性
    // 当一个线程修改了该变量，其他线程能立即看到最新值
    private volatile boolean memoryLoaded = false;
    // 使用读写锁保护缓存的读写操作
    // 读操作可以并发执行，写操作需要独占锁，提高并发性能
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    // 初始化锁，用于保护首次加载缓存的双重检查锁定模式
    private final Object initLock = new Object();

    public CachingUserDao(UserDao sqlDao, ArrayListUserDao memoryCache) {
        this.sqlDao = sqlDao;
        this.memoryCache = memoryCache;
    }

    // 使用双重检查锁定模式确保缓存只被初始化一次
    // 这种模式既保证了线程安全，又避免了每次读取都加锁的性能损耗
    private void ensureMemoryLoaded() {
        if (!memoryLoaded) {
            synchronized (initLock) {
                if (!memoryLoaded) {
                    try {
                        List<User> users = sqlDao.findAllUsers();
                        for (User u : users) {
                            memoryCache.addUser(u);
                        }
                    } catch (Exception e) {
                        throw new DataAccessException("初始化用户缓存失败！", e);
                    }
                    memoryLoaded = true;
                }
            }
        }
    }

    @Override
    public void addUser(User user) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.addUser(user);
            memoryCache.addUser(user);
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public User findByUsername(String username) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.findByUsername(username);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.existsByUsername(username);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAllUsers() {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.findAllUsers();
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }
}
