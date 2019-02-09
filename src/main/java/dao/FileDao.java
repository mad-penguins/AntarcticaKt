package dao;

import models.File;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.FileDBSessionFactoryUtil;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

    public File findByNamePath(String name, String path) {
        Session session = FileDBSessionFactoryUtil.getSessionFactory(userID, password).openSession();
        CriteriaQuery<File> criteriaQuery = session.getCriteriaBuilder().createQuery(File.class);
        Root<File> root = criteriaQuery.from(File.class);
        criteriaQuery.multiselect(root.get("name"), root.get("path"));
        criteriaQuery.where(
                session.getCriteriaBuilder().equal(root.get("name"), name),
                session.getCriteriaBuilder().equal(root.get("path"), path)
        );

        File result = null;
        try {
            result = session.createQuery(criteriaQuery).getSingleResult();
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        } catch (NoResultException ignored) {}

        return result;
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
