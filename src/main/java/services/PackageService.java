package services;

import dao.PackageDao;
import models.File;
import models.Package;

import java.util.List;

public class PackageService extends Service<Package> {

    private PackageDao packageDao;

    public PackageService(int userID, String password) {
        packageDao = new PackageDao(userID, password);
    }

    public Package find(int id) {
        return packageDao.findById(id);
    }

    public void save(Package _package) {
        packageDao.save(_package);
    }

    public void update(Package _package) {
        packageDao.update(_package);
    }

    public void delete(Package _package) {
        packageDao.delete(_package);
    }

    public File findFileById(int id) {
        return packageDao.findFileById(id);
    }

    public List<Package> getAll() {
        return packageDao.getAll();
    }
}
