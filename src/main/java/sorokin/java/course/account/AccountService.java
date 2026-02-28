package sorokin.java.course.account;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.TransactionHelper;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;
import sorokin.java.course.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final TransactionHelper transactionHelper;

    public AccountService(
            AccountProperties accountProperties,
            TransactionHelper transactionHelper
    ) {
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    public Account createAccount(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }
        return transactionHelper.executeInTransaction(session -> {
            User user = session.find(User.class, userId);
            if (user == null) {
                throw new EntityNotFoundException("User not found with ID: " + userId);
            }
            Account account = new Account(accountProperties.getDefaultAmount());
            user.addAccount(account);
            session.persist(account);
            return account;
        });
    }

    public void deposit(int userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive: " + amount);
        }
        transactionHelper.executeInTransaction(session -> {
            var user = session.find(User.class, userId);
            if (user == null) {
                throw new EntityNotFoundException("User not found with ID: " + userId);
            }
            Account account = user.getAccountList().getFirst();
            account.setMoneyAmount(account.getMoneyAmount() + amount);
            session.persist(account);
        });
    }

    public void closeAccount(int userId, int accountId) {
        if (userId <= 0 || accountId <= 0) {
            throw new IllegalArgumentException("Invalid user ID or account ID");
        }
        transactionHelper.executeInTransaction(session -> {
            var user = session.find(User.class, userId);
            if (user == null) {
                throw new EntityNotFoundException("User not found with ID: " + userId);
            }
            List<Account> accountList = user.getAccountList();

            if (accountList.size() == 1) {
                throw new IllegalArgumentException("Cannot close the only account of a user");
            }

            Account accountFrom = accountList.stream()
                    .filter(account -> account.getId() == accountId)
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Account not found with ID: " + accountId));

            int moneyAmount = accountFrom.getMoneyAmount();

            accountList.remove(accountFrom);
            session.remove(accountFrom);

            accountList = user.getAccountList();
            Account accountTo = accountList.getFirst();

            int newBalance = accountTo.getMoneyAmount() + moneyAmount;
            accountTo.setMoneyAmount(newBalance);
            session.persist(user);
        });
    }

//    public Account closeAccount(Integer accountId) {
//        validatePositiveId(accountId, "account id");
//        Account accountToClose = findAccountById(accountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(accountId)));
//        var userId = accountToClose.getUserId();
//        var userAccounts = getUserAccounts(userId);
//        if (userAccounts.size() == 1) {
//            throw new IllegalStateException("Can't close the only one account");
//        }
//        accountMap.remove(accountId);
//
//        var accountToTransferMoney = userAccounts.stream()
//                .filter(it -> it.getId() != accountId)
//                .findFirst()
//                .orElseThrow();
//
//        var newAmount = accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount();
//        accountToTransferMoney.setMoneyAmount(newAmount);
//        return accountToClose;
//    }


    //    public void deposit(Integer toAccountId, Integer amount) {

//        Account account = findAccountById(toAccountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));
//
//        account.setMoneyAmount(account.getMoneyAmount() + amount);
//    }

//    private void validatePositiveId(Integer id, String fieldName) {
//        if (id == null || id <= 0) {
//            throw new IllegalArgumentException(fieldName + " must be > 0");
//        }
//    }
//
//    private void validatePositiveAmount(Integer amount) {
//        if (amount == null || amount <= 0) {
//            throw new IllegalArgumentException("amount must be > 0");
//        }
//    }

}


//
//    public AccountService(AccountProperties accountProperties) {
//        this.idCounter = 0;
//        this.accountMap = new HashMap<>();
//        this.accountProperties = accountProperties;
//    }
//
//    public Account createAccount(User user) {
//        return transactionHelper.executeInTransaction(session -> {
//            Account account = new Account(accountProperties.getDefaultAmount());
//            user.addAccount(account);
//            session.persist(account);
//            session.persist(user);
//            return account;
//        });
//        return new Account(accountProperties.getDefaultAmount());

/// /        return transactionHelper.executeInTransaction(session -> {
/// /            session.persist(account);
/// /            return account;
/// /        });

//    public Account createAccount(User user) {
//        if (user == null) {
//            throw new IllegalArgumentException("user must not be null");
//        }
//        Account newAccount = new Account(user.getId(), accountProperties.getDefaultAmount());
//        return newAccount;
//    }
//
//    public Optional<Account> findAccountById(Integer id) {
//        validatePositiveId(id, "account id");
//        return Optional.ofNullable(accountMap.get(id));
//    }
//
//    public List<Account> getUserAccounts(Integer userId) {
//        return accountMap.values().stream()
//                .filter(it -> userId.equals(it.getUserId()))
//                .toList();
//    }
//
//    public void withdraw(Integer fromAccountId, Integer amount) {
//        validatePositiveId(fromAccountId, "account id");
//        validatePositiveAmount(amount);
//        Account account = findAccountById(fromAccountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));
//
//        if (amount > account.getMoneyAmount()) {
//            throw new IllegalArgumentException(
//                    "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
//                            .formatted(account.getId(), account.getMoneyAmount(), amount)
//            );
//        }
//        account.setMoneyAmount(account.getMoneyAmount() - amount);
//    }
//
//    public void deposit(Integer toAccountId, Integer amount) {
//        validatePositiveId(toAccountId, "account id");
//        validatePositiveAmount(amount);
//        Account account = findAccountById(toAccountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));
//
//        account.setMoneyAmount(account.getMoneyAmount() + amount);
//    }
//
//    public Account closeAccount(Integer accountId) {
//        validatePositiveId(accountId, "account id");
//        Account accountToClose = findAccountById(accountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(accountId)));
//        var userId = accountToClose.getUserId();
//        var userAccounts = getUserAccounts(userId);
//        if (userAccounts.size() == 1) {
//            throw new IllegalStateException("Can't close the only one account");
//        }
//        accountMap.remove(accountId);
//
//        var accountToTransferMoney = userAccounts.stream()
//                .filter(it -> it.getId() != accountId)
//                .findFirst()
//                .orElseThrow();
//
//        var newAmount = accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount();
//        accountToTransferMoney.setMoneyAmount(newAmount);
//        return accountToClose;
//    }
//
//    public void transfer(int fromAccountId, int toAccountId, int amount) {
//        validatePositiveId(fromAccountId, "source account id");
//        validatePositiveId(toAccountId, "target account id");
//        validatePositiveAmount(amount);
//        if (fromAccountId == toAccountId) {
//            throw new IllegalArgumentException("source and target account id must be different");
//        }
//        Account accountFrom = findAccountById(fromAccountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));
//        Account accountTo = findAccountById(toAccountId)
//                .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));
//
//        if (amount > accountFrom.getMoneyAmount()) {
//            throw new IllegalArgumentException(
//                    "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
//                            .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
//            );
//        }
//        accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amount);
//
//        int amountToTransfer = accountTo.getUserId() == accountFrom.getUserId()
//                ? amount
//                : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
//        accountTo.setMoneyAmount(accountTo.getMoneyAmount() + amountToTransfer);
//    }
//
//    private void validatePositiveId(Integer id, String fieldName) {
//        if (id == null || id <= 0) {
//            throw new IllegalArgumentException(fieldName + " must be > 0");
//        }
//    }
//
//    private void validatePositiveAmount(Integer amount) {
//        if (amount == null || amount <= 0) {
//            throw new IllegalArgumentException("amount must be > 0");
//        }
//    }

