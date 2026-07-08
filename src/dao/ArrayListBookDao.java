package dao;

import model.Book;
import exception.BookNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ArrayListBookDao implements BookDao {
    private final List<Book> books = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void addBook(Book book) {
        book.setId(nextId++);
        books.add(book);
    }

    @Override
    public void deleteBook(int id) {
        Book book = findBookById(id);
        books.remove(book);
    }

    @Override
    public void updateBook(Book book) {
        Book existing = findBookById(book.getId());
        existing.setBookname(book.getBookname());
        existing.setAuthor(book.getAuthor());
        existing.setPrice(book.getPrice());
    }

    @Override
    public Book findBookById(int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        throw new BookNotFoundException("未找到编号为 " + id + " 的图书！");
    }

    @Override
    public Book findBookByName(String bookname) {
        for (Book book : books) {
            if (book.getBookname().equals(bookname)) {
                return book;
            }
        }
        throw new BookNotFoundException("未找到书名为「" + bookname + "」的图书！");
    }

    @Override
    public List<Book> findAllBooks() {
        return new ArrayList<>(books);
    }

    @Override
    public boolean exists(String bookname, String author, double price) {
        for (Book book : books) {
            if (book.getBookname().equals(bookname) && book.getAuthor().equals(author) && book.getPrice() == price) {
                return true;
            }
        }
        return false;
    }
}
