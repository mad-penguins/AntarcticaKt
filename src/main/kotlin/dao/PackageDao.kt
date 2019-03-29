package dao

import models.File
import models.Package
import org.hibernate.HibernateException
import utils.SessionFactoryUtil
import utils.UIUtil

class PackageDao(private val userID: Int, private val password: String) : Dao<Package>() {

    override val all: List<Package>?
        get() = try {
            SessionFactoryUtil.getSessionFactory(userID, password)
                ?.openSession()?.createQuery("from Package")?.list() as List<Package>
        } catch (e: Exception) {
            when (e) {
                is org.hibernate.HibernateException, is javax.persistence.PersistenceException -> {
                    UIUtil.showHibernateError(e.toString())
                } else -> { e.printStackTrace() }
            }
            null
        }

    override fun findById(id: Int): Package? {
        return try {
            SessionFactoryUtil.getSessionFactory(userID, password)?.openSession()?.get(Package::class.java, id)
        } catch (e: Exception) {
            when (e) {
                is org.hibernate.HibernateException, is javax.persistence.PersistenceException -> {
                    UIUtil.showHibernateError(e.toString())
                } else -> { e.printStackTrace() }
            }
            null
        }
    }

    override fun save(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session?.beginTransaction()
        session?.saveOrUpdate(obj)
        session.flush()
        session.clear()
        tx?.commit()
        session?.close()
    }

    override fun update(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session?.beginTransaction()
        session?.update(obj)
        tx?.commit()
        session.flush()
        session.clear()
        session?.close()
    }

    override fun delete(obj: Package) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        try {
            val tx = session.beginTransaction()
            val query = session.createQuery("delete from Package p where p.id=:i")
            query.setParameter("i", obj.id).executeUpdate()
            session.flush()
            session.clear()
            tx.commit()
            session.close()
        } catch (he: HibernateException) {
            session.transaction.rollback()
            he.printStackTrace()
        } finally {
            session.close()
        }
    }

    fun findFileById(id: Int): File? {
        return try {
            SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession().get(File::class.java, id)
        } catch (e: Exception) {
            when (e) {
                is org.hibernate.HibernateException, is javax.persistence.PersistenceException -> {
                    UIUtil.showHibernateError(e.toString())
                } else -> { e.printStackTrace() }
            }
            null
        }
    }
}
