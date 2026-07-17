package service;

import dao.BookDao;
import dao.BorrowRecordDao;
import dao.UserDao;
import model.Book;
import model.BorrowRecord;
import model.User;
import exception.StockInsufficientException;
import exception.AdminBorrowNotAllowedException;
import exception.BorrowLimitExceededException;
import exception.UserNotFoundException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BorrowService {
    private final BookDao bookDao;
    private final UserDao userDao;
    private final BorrowRecordDao borrowRecordDao;
    // 使用ConcurrentHashMap存储图书对应的锁，实现按图书ID细粒度加锁
    // 这样不同图书的借阅操作可以并发执行，同一本图书的借阅操作会被串行化
    private final ConcurrentHashMap<Integer, ReentrantLock> bookLocks = new ConcurrentHashMap<>();

    public BorrowService(BookDao bookDao, UserDao userDao, BorrowRecordDao borrowRecordDao) {
        this.bookDao = bookDao;
        this.userDao = userDao;
        this.borrowRecordDao = borrowRecordDao;
    }

    public void borrowBook(String username, int bookId) {
        User user;
        try {
            user = userDao.findByUsername(username);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("用户「" + username + "」不存在！");
        }

        if (!user.canBorrowBook()) {
            throw new AdminBorrowNotAllowedException("管理员不能借阅图书！");
        }

        // 获取对应图书的锁对象，如果不存在则创建一个新的锁
        // 这确保了同一本图书的所有借阅操作都使用同一个锁
        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, k -> new ReentrantLock());
        // 在整个借阅流程中加锁，确保库存检查和扣减的原子性
        lock.lock();
        try {
            Book book = bookDao.findBookById(bookId);

            if (bookDao.getStock(bookId) <= 0) {
                throw new StockInsufficientException("图书「" + book.getBookname() + "」库存不足，无法借阅！");
            }

            int currentCount = borrowRecordDao.getBorrowCount(username);
            if (currentCount >= user.getMaxBorrowCount()) {
                throw new BorrowLimitExceededException("您已达到最大借阅数量（" + user.getMaxBorrowCount() + " 本），无法继续借阅！");
            }

            borrowRecordDao.borrowBook(username, bookId);
        } finally {
            // 无论借阅成功与否，都必须释放锁，避免死锁
            lock.unlock();
        }
    }

    public void returnBook(String username, int recordId) {
        borrowRecordDao.returnBook(recordId);
    }

    public List<BorrowRecord> getUnreturnedRecords(String username) {
        return borrowRecordDao.findUnreturnedByUsername(username);
    }

    public List<BorrowRecord> getBorrowRecords(String username) {
        return borrowRecordDao.findByUsername(username);
    }
}
