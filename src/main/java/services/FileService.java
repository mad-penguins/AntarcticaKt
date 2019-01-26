package services;

import dao.FileDao;
import models.File;

import java.util.List;

public class FileService extends Service<File> {

    private FileDao fileDao;

    public FileService(int userID, String password) {
        fileDao = new FileDao(userID, password);
    }

    public File find(int id) {
        return fileDao.findById(id);
    }

    public void save(File file) {
        fileDao.save(file);
    }

    public void update(File file) {
        fileDao.update(file);
    }

    public void delete(File file) {
        fileDao.delete(file);
    }

    public List<File> getAll() {
        return fileDao.getAll();
    }
}
