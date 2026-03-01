package sorokin.java.course.account;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;
import sorokin.java.course.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.List;


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
        userIdValidate(userId);
        return transactionHelper.executeInTransaction(session -> {
            User user = session.find(User.class, userId);
            userExistValidate(userId, user);
            Account account = new Account(accountProperties.getDefaultAmount());
            user.addAccount(account);
            session.persist(account);
            return account;
        });
    }

    public void deposit(int userId, int amount) {
        validatePositiveAmount(amount);
        transactionHelper.executeInTransaction(session -> {
            var user = session.find(User.class, userId);
            userExistValidate(userId, user);
            Account account = user.getAccountList().getFirst();
            account.setMoneyAmount(account.getMoneyAmount() + amount);
            session.persist(account);
        });
    }

    public void withdraw(int userId, int accountId, int amount) {
        userIdAccountIdValidate(userId, accountId);
        validatePositiveAmount(amount);
        transactionHelper.executeInTransaction(session -> {
            var user = session.find(User.class, userId);
            userExistValidate(userId, user);
            List<Account> accountList = user.getAccountList();
            Account accountFrom = accountList.stream()
                    .filter(account -> account.getId() == accountId)
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Account not found with ID: " + accountId));
            int currentBalance = accountFrom.getMoneyAmount();
            if (currentBalance < amount) {
                throw new IllegalArgumentException(
                        "Insufficient funds. Balance: " + currentBalance + ", requested: " + amount);
            }
            accountFrom.setMoneyAmount(currentBalance - amount);
        });
    }



    public void closeAccount(int userId, int accountId) {
        userIdAccountIdValidate(userId, accountId);
        transactionHelper.executeInTransaction(session -> {
            var user = session.find(User.class, userId);
            userExistValidate(userId, user);
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

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);

        transactionHelper.executeInTransaction(session -> {
            Account fromAccount = session.find(Account.class, fromAccountId);
            Account toAccount = session.find(Account.class, toAccountId);

            if(fromAccount.getId() == toAccount.getId()) {
                throw new IllegalArgumentException("Transfers from the same account number are prohibited");
            }

            if (fromAccount.getMoneyAmount() < amount) {
                throw new IllegalArgumentException("Not enough money to the transfer");
            }

            if (fromAccount.getUser().getId() == toAccount.getUser().getId()) {
                toAccount.setMoneyAmount(toAccount.getMoneyAmount() + amount);
                fromAccount.setMoneyAmount(fromAccount.getMoneyAmount() - amount);
            }
            if (fromAccount.getUser().getId() != toAccount.getUser().getId()) {
                int transferWithCommission = Math.toIntExact(Math.round(amount * (1 - accountProperties.getTransferCommission())));
                toAccount.setMoneyAmount(toAccount.getMoneyAmount() + transferWithCommission);
                fromAccount.setMoneyAmount(fromAccount.getMoneyAmount() - amount);
            }

        });
    }

    private void validatePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }

    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }

    private static void userIdValidate(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }
    }

    private static void userExistValidate(int userId, User user) {
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
    }

    private static void userIdAccountIdValidate(int userId, int accountId) {
        if (userId <= 0 || accountId <= 0) {
            throw new IllegalArgumentException("Invalid user ID or account ID");
        }
    }

}



