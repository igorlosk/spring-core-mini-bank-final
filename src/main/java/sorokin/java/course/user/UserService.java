package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.TransactionHelper;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountProperties;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.user.User;

import java.util.*;

@Component
public class UserService {

    private final AccountService accountService;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;
    private final AccountProperties accountProperties;


    public UserService(
            AccountService accountService,
            SessionFactory sessionFactory,
            TransactionHelper transactionHelper,
            AccountProperties accountProperties) {
        this.accountService = accountService;
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.accountProperties = accountProperties;
    }

    public User createUser(String login) {
        var user = new User(login);
        return transactionHelper.executeInTransaction(session -> {
            Account account = new Account(accountProperties.getDefaultAmount());
            user.addAccount(account);
            session.persist(user);
            return user;
        });
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    SELECT u FROM User u
                    LEFT JOIN FETCH u.accountList
                    """, User.class).list();
        }
    }
}
