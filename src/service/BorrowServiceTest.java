package service;

import dao.BookDao;
import dao.BorrowRecordDao;
import dao.UserDao;
import model.*;
import exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowServiceTest {

    private BookDao bookDao;
    private UserDao userDao;
    private BorrowRecordDao borrowRecordDao;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        bookDao = mock(BookDao.class);
        userDao = mock(UserDao.class);
        borrowRecordDao = mock(BorrowRecordDao.class);
        borrowService = new BorrowService(bookDao, userDao, borrowRecordDao);
    }

    @Test
    @DisplayName("正常借阅应成功")
    void borrowBook_normal_shouldSucceed() {
        NormalUser user = new NormalUser("alice", "123456");
        Book book = new Book(1, "Java", "张三", 59.9, 5);
        when(userDao.findByUsername("alice")).thenReturn(user);
        when(bookDao.findBookById(1)).thenReturn(book);
        when(bookDao.getStock(1)).thenReturn(5);
        when(borrowRecordDao.getBorrowCount("alice")).thenReturn(0);

        assertDoesNotThrow(() -> borrowService.borrowBook("alice", 1));

        verify(borrowRecordDao).borrowBook("alice", 1);
    }

    @Test
    @DisplayName("库存不足应抛出异常")
    void borrowBook_noStock_shouldThrow() {
        NormalUser user = new NormalUser("alice", "123456");
        Book book = new Book(1, "Java", "张三", 59.9, 0);
        when(userDao.findByUsername("alice")).thenReturn(user);
        when(bookDao.findBookById(1)).thenReturn(book);
        when(bookDao.getStock(1)).thenReturn(0);

        assertThrows(StockInsufficientException.class,
                () -> borrowService.borrowBook("alice", 1));
    }
}
