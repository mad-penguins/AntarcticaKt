package services

import dao.FileDao
import models.File

class FileService(userID: Int, password: String) : Service<File>() {

    private val fileDao: FileDao = FileDao(userID, password)

    override fun find(id: Int): File? {
        return fileDao.findById(id)
    }

    fun find(name: String, path: String) : File? {
        return fileDao.findByNamePath(name, path)
    }

    override fun save(obj: File) {
        if (fileDao.findByNamePath(obj.name, obj.path!!) == null) {
            fileDao.save(obj)
        }
    }

    override fun update(obj: File) {
        fileDao.update(obj)
    }

    override fun delete(obj: File) {
        fileDao.delete(obj)
    }

    override fun getAll(): List<File> {
        return fileDao.all
    }
}
