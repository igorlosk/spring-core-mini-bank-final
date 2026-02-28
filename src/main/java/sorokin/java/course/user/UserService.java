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

    //
//    public User createUser(String login) {
//        String normalizedLogin = validateLogin(login);
//        if (takenLogins.contains(normalizedLogin)) {
//            throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
//        }
//
//        idCounter++;
//        var user = new User(idCounter, normalizedLogin, new ArrayList<>());
//        var defaultAccount = accountService.createAccount(user);
//        user.getAccountList().add(defaultAccount);
//
//        userMap.put(idCounter, user);
//        takenLogins.add(normalizedLogin);
//        return user;
//    }
//
    public User findUserById(Integer id) {
        try (Session session = sessionFactory.openSession();) {
            return session.find(User.class, id);
        }
    }
//    public User findUserById(Integer id) {
//        if (id == null || id <= 0) {
//            throw new IllegalArgumentException("user id must be > 0");
//        }
//        var user = userMap.get(id);
//        if (user == null) {
//            throw new IllegalArgumentException("No such user with id=%s".formatted(id));
//        }
//        return user;
//    }
//

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
//            session.beginTransaction();
//            return session.createQuery("from User u", User.class).getResultList();
            return session.createQuery("""
                    SELECT u FROM User u
                    LEFT JOIN FETCH u.accountList
                    """, User.class).list();
        }
    }

//    public List<User> findAll() {
//        return userMap.values().stream().toList();
//    }
//
//    private String validateLogin(String login) {
//        if (login == null || login.isBlank()) {
//            throw new IllegalArgumentException("login must not be blank");
//        }
//        return login.trim();
//    }
}
