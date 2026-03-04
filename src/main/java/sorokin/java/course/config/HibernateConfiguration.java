package sorokin.java.course.config;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.context.annotation.Bean;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

@org.springframework.context.annotation.Configuration
public class HibernateConfiguration {


    Configuration configuration = new Configuration();

    @Bean
    public SessionFactory sessionFactory() {
        Configuration configuration = new Configuration();
        configuration
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .addPackage("com.project")
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/postgres")
                .setProperty("hibernate.connection.username", "postgres")
                .setProperty("hibernate.connection.password", "root")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "update");
        return configuration.buildSessionFactory();


    }

}
