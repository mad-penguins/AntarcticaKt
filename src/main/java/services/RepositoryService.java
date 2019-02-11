package services;

import dao.RepositoryDao;
import models.Repository;

import java.util.List;

public class RepositoryService extends Service<Repository> {

    private RepositoryDao repoDao;

    public RepositoryService(int userID, String password) {
        repoDao = new RepositoryDao(userID, password);
    }

    public Repository find(int id) {
        return repoDao.findById(id);
    }

    public Repository find(String url) {
        return repoDao.findbyURL(url);
    }

    public void save(Repository repo) {
        repoDao.save(repo);
    }

    public void update(Repository repo) {
        repoDao.update(repo);
    }

    public void delete(Repository repo) {
        repoDao.delete(repo);
    }

    public Package findPackageById(int id) {
        return repoDao.findPackageById(id);
    }

    public List<Repository> getAll() {
        return repoDao.getAll();
    }
}
