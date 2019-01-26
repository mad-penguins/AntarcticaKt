package dao;

import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.UserDBSessionFactoryUtil;
import java.util.List;

public class UserDao extends Dao<User> {

    public User findById(int id) {
        return UserDBSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);
    }

    public void save(User user) {
        Session session = UserDBSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(user);
        tx.commit();
        session.close();
    }

    public void update(User user) {
        Session session = UserDBSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(user);
        tx.commit();
        session.close();
    }

    public void delete(User user) {
        Session session = UserDBSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.delete(user);
        tx.commit();
        session.close();
    }

    public List<User> getAll() {
        return (List<User>) UserDBSessionFactoryUtil.getSessionFactory().openSession().createQuery("from User").list();
    }

}
