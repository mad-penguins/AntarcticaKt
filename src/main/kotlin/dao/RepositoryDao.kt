package dao

import models.Repository
import org.hibernate.NonUniqueResultException
import utils.SessionFactoryUtil

import javax.persistence.NoResultException

class RepositoryDao(private val userID: Int, private val password: String) : Dao<Repository>() {

    override val all: List<Repository>?
        get() = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
                .createQuery("from Repository").list() as List<Repository>

    override fun findById(id: Int): Repository {
        return SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession().get(Repository::class.java, id)
    }

    fun findByURL(url: String): Repository? {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val criteriaQuery = session.criteriaBuilder.createQuery(Repository::class.java)
        val root = criteriaQuery.from(Repository::class.java)
        criteriaQuery.select(root)
        criteriaQuery.where(
                session.criteriaBuilder.equal(root.get<Any>("url"), url)
        )

        var result: Repository? = null
        try {
            result = session.createQuery(criteriaQuery).singleResult
        } catch (e: NonUniqueResultException) {
            e.printStackTrace()
        } catch (ignored: NoResultException) {
        }

        return result
    }

    override fun save(obj: Repository) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session.beginTransaction()
        session.save(obj)
        tx.commit()
        session.close()
    }

    override fun update(obj: Repository) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session.beginTransaction()
        session.update(obj)
        tx.commit()
        session.close()
    }

    override fun delete(obj: Repository) {
        val session = SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession()
        val tx = session.beginTransaction()
        session.delete(obj)
        tx.commit()
        session.close()
    }

    fun findPackageById(id: Int): Package {
        return SessionFactoryUtil.getSessionFactory(userID, password)!!.openSession().get(Package::class.java, id)
    }

}
