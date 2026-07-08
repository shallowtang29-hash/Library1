// 文件路径: src/dao/UserDao.java
package dao;

import model.User;

public interface UserDao {
    void addUser(User user);

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
