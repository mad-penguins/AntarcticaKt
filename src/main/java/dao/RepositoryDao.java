package dao;

import models.File;
import models.Repository;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.SessionFactoryUtil;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class RepositoryDao extends Dao<Repository> {

    private int userID;
    private String password;

    public RepositoryDao(int userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public Repository findById(int id) {
        return SessionFactoryUtil.getSessionFactory(userID, password).openSession().get(Repository.class, id);
    }

    public Repository findbyURL(String url) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        CriteriaQuery<Repository> criteriaQuery = session.getCriteriaBuilder().createQuery(Repository.class);
        Root<Repository> root = criteriaQuery.from(Repository.class);
        criteriaQuery.select(root);
        criteriaQuery.where(
                session.getCriteriaBuilder().equal(root.get("url"), url)
        );

        Repository result = null;
        try {
            result = session.createQuery(criteriaQuery).getSingleResult();
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        } catch (NoResultException ignored) {}

        return result;
    }

    public void save(Repository repo) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.save(repo);
        tx.commit();
        session.close();
    }

    public void update(Repository repo) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.update(repo);
        tx.commit();
        session.close();
    }

    public void delete(Repository repo) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.delete(repo);
        tx.commit();
        session.close();
    }

    public Package findPackageById(int id) {
        return SessionFactoryUtil.getSessionFactory(userID, password).openSession().get(Package.class, id);
    }

    public List<Repository> getAll() {
        return (List<Repository>)
                SessionFactoryUtil.getSessionFactory(userID, password).openSession()
                        .createQuery("from Repository").list();
    }

    
}
