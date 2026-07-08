// 文件路径: src/service/UserService.java
package service;

import dao.UserDao;
import model.Admin;
import model.NormalUser;
import model.User;
import exception.UserDuplicateException;
import exception.UserNotFoundException;
import exception.PasswordException;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void register(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new UserDuplicateException("用户名不能为空！");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordException("密码不能为空！");
        }
        if (password.length() < 6) {
            throw new PasswordException("密码长度不能少于6位！");
        }
        if (userDao.existsByUsername(username)) {
            throw new UserDuplicateException("用户名「" + username + "」已存在！");
        }

        User user;
        if ("admin".equalsIgnoreCase(role)) {
            user = new Admin(username, password);
        } else {
            user = new NormalUser(username, password);
        }
        userDao.addUser(user);
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new UserNotFoundException("用户名不能为空！");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordException("密码不能为空！");
        }
        User user = userDao.findByUsername(username);
        if (!user.getPassword().equals(password)) {
            throw new PasswordException("密码错误！");
        }
        return user;
    }
}
