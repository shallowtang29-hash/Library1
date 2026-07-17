package service;

import dao.BookDao;
import model.Book;
import exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private BookDao bookDao;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookDao = mock(BookDao.class);
        bookService = new BookService(bookDao);
    }

    @Test
    @DisplayName("正常添加图书应成功")
    void addBook_validInput_shouldSucceed() {
        when(bookDao.exists("Java编程", "张三", 59.9)).thenReturn(false);

        assertDoesNotThrow(() -> bookService.addBook("Java编程", "张三", 59.9));

        verify(bookDao).addBook(any(Book.class));
    }

    @Test
    @DisplayName("重复图书应抛出异常")
    void addBook_duplicate_shouldThrow() {
        when(bookDao.exists("Java编程", "张三", 59.9)).thenReturn(true);

        assertThrows(BookDuplicateException.class,
                () -> bookService.addBook("Java编程", "张三", 59.9));
    }
}
