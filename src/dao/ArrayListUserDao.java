// 文件路径: src/dao/ArrayListUserDao.java
package dao;

import model.User;
import exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ArrayListUserDao implements UserDao {
    private final List<User> users = new ArrayList<>();

    @Override
    public void addUser(User user) {
        users.add(user);
    }

    @Override
    public User findByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        throw new UserNotFoundException("用户「" + username + "」不存在！");
    }

    @Override
    public boolean existsByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
