package services

import dao.PackageDao
import models.File
import models.Package

class PackageService(userID: Int, password: String) : Service<Package>(userID, password) {

    private var packageDao: PackageDao = PackageDao(userID, password)

    override fun reload(): PackageService {
        packageDao = PackageDao(userID, password)
        return this
    }

    override fun find(id: Int): Package? {
        return packageDao.findById(id)
    }

    override fun save(obj: Package) {
        packageDao.save(obj)
    }

    override fun update(obj: Package) {
        packageDao.update(obj)
    }

    override fun delete(obj: Package) {
        packageDao.delete(obj)
    }

    fun findFileById(id: Int): File {
        return packageDao.findFileById(id)
    }

    override fun getAll(): List<Package>? {
        return packageDao.all?.drop(1)
    }
}
