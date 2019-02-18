package dao

import models.File
import models.Package
import utils.SessionFactoryUtil

class PackageDao(private val userID: Int, private val password: String) : Dao<Package>() {

    override val all: List<Package>
        get() = SessionFactoryUtil.getSessionFactory(userID, password)
                ?.openSession()?.createQuery("from Package")?.list() as List<Package>

    override fun findById(id: Int): Package? {
        return SessionFactoryUtil.getSessionFactory(userID, password)?.openSession()?.get(Package::class.java, id)
    }

    override fun save(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session?.beginTransaction()
        session?.save(obj)
        tx?.commit()
        session?.close()
    }

    override fun update(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session?.beginTransaction()
        session?.update(obj)
        tx?.commit()
        session?.close()
    }

    override fun delete(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session.beginTransaction()
        session.delete(obj)
        tx.commit()
        session.close()
    }

    fun findFileById(id: Int): File {
        return SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession().get(File::class.java, id)
    }


}
