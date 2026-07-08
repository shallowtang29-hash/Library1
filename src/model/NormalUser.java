package model;

public class NormalUser extends User {

    public NormalUser(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "普通用户";
    }

    @Override
    public boolean canBorrowBook() {
        return true;
    }

    @Override
    public int getMaxBorrowCount() {
        return 5;
    }
}
