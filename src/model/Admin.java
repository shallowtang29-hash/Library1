package model;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "管理员";
    }

    @Override
    public boolean canBorrowBook() {
        return false;
    }

    @Override
    public int getMaxBorrowCount() {
        return 0;
    }
}
