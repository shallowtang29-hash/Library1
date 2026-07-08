package model;

public class Book {
    private int id;
    private String bookname;
    private String author;
    private int price;

    public Book() {
    }

    public Book(int id, String bookname, String author, double price) {
        this.id = id;
        this.bookname = bookname;
        this.author = author;
        this.price = (int)(price * 100);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price / 100.0;
    }

    public void setPrice(double price) {
        this.price = (int)(price * 100);
    }

    public String toString() {
        return "Book [编号：" + id + ", 书名：" + bookname + ", 作者：" + author + ", 价格：" + String.format("%.2f", price / 100.0) + "元]";
    }
}