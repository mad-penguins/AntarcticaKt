package services

import dao.RepositoryDao
import models.Repository

class RepositoryService(userID: Int, password: String) : Service<Repository>(userID, password) {

    private var repoDao: RepositoryDao = RepositoryDao(userID, password)

    override fun reload(): RepositoryService {
        repoDao = RepositoryDao(userID, password)
        return this
    }

    override fun find(id: Int): Repository {
        return repoDao.findById(id)
    }

    fun find(url: String): Repository? {
        return repoDao.findByURL(url)
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
