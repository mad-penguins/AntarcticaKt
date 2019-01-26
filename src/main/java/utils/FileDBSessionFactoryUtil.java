package utils;

import models.File;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class FileDBSessionFactoryUtil {
    private static SessionFactory sessionFactory;

    private FileDBSessionFactoryUtil() {

    }

    public static SessionFactory getSessionFactory(int id, String password) {
        if (sessionFactory == null) {
            try {
                Properties prop = new Properties();
                prop.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/id"+ id);
                prop.setProperty("dialect", "org.hibernate.dialect.MariaDBDialect");
                prop.setProperty("hibernate.connection.username", "id"+ id);
                prop.setProperty("hibernate.connection.password", password);
                prop.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
                prop.setProperty("show_sql", String.valueOf(true));

                Configuration configuration = new Configuration().addProperties(prop);
                configuration.addAnnotatedClass(File.class);
                StandardServiceRegistryBuilder builder =
                        new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (Exception e) {
                System.out.println("Exception! " + e);
            }
        }
        return sessionFactory;
    }
}
