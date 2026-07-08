package dao;

import model.Book;
import java.util.List;

public interface BookDao {
    void addBook(Book book);

    void deleteBook(int id);

    void updateBook(Book book);

    Book findBookById(int id);

    Book findBookByName(String bookname);

    List<Book> findAllBooks();

    boolean exists(String bookname, String author, double price);

}
