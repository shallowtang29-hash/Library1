package dao;

import model.Book;
import exception.BookNotFoundException;
import exception.DataAccessException;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachingBookDao implements BookDao {

    private final BookDao sqlDao;
    private final ArrayListBookDao memoryCache;
    // volatile关键字确保memoryLoaded变量在多线程间的可见性
    // 当一个线程修改了该变量，其他线程能立即看到最新值
    private volatile boolean memoryLoaded = false;
    // 使用读写锁保护缓存的读写操作
    // 读操作可以并发执行，写操作需要独占锁，提高并发性能
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    // 初始化锁，用于保护首次加载缓存的双重检查锁定模式
    private final Object initLock = new Object();

    public CachingBookDao(BookDao sqlDao, ArrayListBookDao memoryCache) {
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
                        List<Book> books = sqlDao.findAllBooks();
                        for (Book b : books) {
                            memoryCache.addBook(copyBook(b));
                        }
                    } catch (Exception e) {
                        throw new DataAccessException("初始化缓存失败！", e);
                    }
                    memoryLoaded = true;
                }
            }
        }
    }

    private Book copyBook(Book src) {
        return new Book(src.getId(), src.getBookname(), src.getAuthor(), src.getPrice(), src.getStock());
    }

    @Override
    public void addBook(Book book) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.addBook(book);
            memoryCache.addBook(copyBook(book));
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteBook(int id) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.deleteBook(id);
            try { memoryCache.deleteBook(id); } catch (Exception ignored) {}
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void updateBook(Book book) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.updateBook(book);
            try {
                memoryCache.updateBook(book);
            } catch (BookNotFoundException e) {
                memoryCache.addBook(copyBook(book));
            }
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Book findBookById(int id) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.findBookById(id);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Book findBookByName(String bookname) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.findBookByName(bookname);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Book> findAllBooks() {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.findAllBooks();
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.searchBooks(keyword);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean exists(String bookname, String author, double price) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.exists(bookname, author, price);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int getStock(int bookId) {
        ensureMemoryLoaded();
        // 读操作只需要获取读锁，允许多个线程同时读取
        rwLock.readLock().lock();
        try {
            return memoryCache.getStock(bookId);
        } finally {
            // 释放读锁，避免死锁
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void decreaseStock(int bookId) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.decreaseStock(bookId);
            try { memoryCache.decreaseStock(bookId); } catch (Exception ignored) {}
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void increaseStock(int bookId, int amount) {
        // 写操作需要获取写锁，确保数据一致性
        rwLock.writeLock().lock();
        try {
            sqlDao.increaseStock(bookId, amount);
            try { memoryCache.increaseStock(bookId, amount); } catch (Exception ignored) {}
        } finally {
            // 释放写锁，避免死锁
            rwLock.writeLock().unlock();
        }
    }
}
