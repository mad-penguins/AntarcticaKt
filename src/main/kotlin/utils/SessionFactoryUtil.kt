package utils

import models.File
import models.Package
import models.Repository
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import java.util.*

object SessionFactoryUtil {
    private var sessionFactory: SessionFactory? = null
    private var useSSL = false

    fun getSessionFactory(id: Int, password: String, useSSL: Boolean = this.useSSL): SessionFactory? {
        if (sessionFactory == null) {
            try {
                val prop = Properties()
                prop.setProperty(
                        "hibernate.connection.url",
                        "jdbc:mysql://localhost:3306/id$id${if (useSSL) "?useSSL=true" else ""}"
                )
                prop.setProperty("dialect", "org.hibernate.dialect.MariaDBDialect")
                prop.setProperty("hibernate.connection.username", "id$id")
                prop.setProperty("hibernate.connection.password", password)
                if (useSSL) {
                    prop.setProperty("hibernate.connection.verifyServerCertificate", "false") // TODO: delete this workaround when normally signed certificate will be present
                    prop.setProperty("hibernate.connection.requireSSL", "true")
                }
                prop.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
                prop.setProperty("show_sql", true.toString())

                val configuration = Configuration().addProperties(prop)
                configuration.addAnnotatedClass(File::class.java)
                configuration.addAnnotatedClass(Package::class.java)
                configuration.addAnnotatedClass(Repository::class.java)
                val builder = StandardServiceRegistryBuilder().applySettings(configuration.properties)
                sessionFactory = configuration.buildSessionFactory(builder.build())
            } catch (e: Exception) {
                println("Exception! $e")
            }

        }
        return sessionFactory
    }
}
