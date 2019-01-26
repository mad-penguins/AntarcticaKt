package dao;

import models.File;
import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.FileDBSessionFactoryUtil;
import utils.UserDBSessionFactoryUtil;

import java.util.List;

public class FileDao extends Dao<File> {

    private int userID;
    private String password;

    public FileDao(int userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public File findById(int id) {
        return FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession().get(File.class, id);
    }

    public void save(File file) {
        Session session = FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.save(file);
        tx.commit();
        session.close();
    }

    public void update(File file) {
        Session session = FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.update(file);
        tx.commit();
        session.close();
    }

    public void delete(File file) {
        Session session = FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.delete(file);
        tx.commit();
        session.close();
    }

    public List<File> getAll() {
        return (List<File>) FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession().createQuery("from File").list();
    }
}
