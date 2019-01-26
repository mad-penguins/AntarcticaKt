package services;

import dao.UserDao;
import models.User;

import java.util.List;

public class UserService extends Service<User> {
    public UserService() {

    }

    private UserDao usersDao = new UserDao();

    public User find(int id) {
        return usersDao.findById(id);
    }
    public void save(User user) {
        usersDao.save(user);
    }

    public void update(User user) {
        usersDao.update(user);
    }

    public void delete(User user) {
        usersDao.delete(user);
    }

    public List<User> getAll() {
        return usersDao.getAll();
    }
}
