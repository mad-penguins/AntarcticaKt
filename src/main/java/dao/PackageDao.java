package dao;

import models.File;
import models.Package;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.SessionFactoryUtil;

import java.util.List;

public class PackageDao extends Dao<Package> {

    private int userID;
    private String password;

    public PackageDao(int userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public Package findById(int id) {
        return SessionFactoryUtil.getSessionFactory(userID, password).openSession().get(Package.class, id);
    }

    public void save(Package _package) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.save(_package);
        tx.commit();
        session.close();
    }

    public void update(Package _package) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.update(_package);
        tx.commit();
        session.close();
    }

    public void delete(Package _package) {
        Session session = SessionFactoryUtil.getSessionFactory(userID, password).openSession();
        Transaction tx = session.beginTransaction();
        session.delete(_package);
        tx.commit();
        session.close();
    }

    public File findFileById(int id) {
        return SessionFactoryUtil.getSessionFactory(userID, password).openSession().get(File.class, id);
    }

    public List<Package> getAll() {
        return (List<Package>)
                SessionFactoryUtil.getSessionFactory(userID, password)
                        .openSession().createQuery("from Package").list();
    }

    
}
