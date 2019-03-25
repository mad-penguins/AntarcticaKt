package services

import dao.RepositoryDao
import models.Repository

class RepositoryService(userID: Int, password: String) : Service<Repository>() {

    private val repoDao: RepositoryDao = RepositoryDao(userID, password)

    override fun find(id: Int): Repository {
        return repoDao.findById(id)
    }

    fun find(url: String): Repository? {
        return repoDao.findbyURL(url)
    }

    override fun save(obj: Repository) {
        repoDao.save(obj)
    }

    override fun update(obj: Repository) {
        repoDao.update(obj)
    }

    override fun delete(obj: Repository) {
        repoDao.delete(obj)
    }

    fun findPackageById(id: Int): Package {
        return repoDao.findPackageById(id)
    }

    override fun getAll(): List<Repository>? {
        return repoDao.all?.drop(1)
    }
}
