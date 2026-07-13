/*
 * EXERCISE 2: Bank Account System
 * Concepts: Encapsulation, Inheritance, Method Overriding
 *
 * TASK:
 * 1. Complete the base class Account with private fields: accountNumber,
 *    ownerName, balance. Add getters, and a deposit(double amount) method
 *    that adds to the balance (validate amount > 0).
 * 2. Add a withdraw(double amount) method in Account with basic validation
 *    (cannot withdraw more than balance). Make it so subclasses CAN override
 *    this behavior (don't mark it final).
 * 3. Create SavingsAccount extends Account:
 *    - has an interest rate (e.g., 0.04 for 4%)
 *    - add method applyInterest() that adds balance * rate to balance
 *    - override withdraw() so it blocks withdrawal if balance would drop
 *      below a fixed minimum balance (e.g., 500)
 * 4. Create CurrentAccount extends Account:
 *    - has an overdraft limit (e.g., 1000)
 *    - override withdraw() so it allows the balance to go negative,
 *      but not past -overdraftLimit
 * 5. In main(), create one of each account type, deposit/withdraw from
 *    each, and print results. Show that calling withdraw() through a
 *    base Account reference still triggers the correct subclass behavior
 *    (polymorphism).
 *
 * BONUS:
 * - Add a printStatement() method that subclasses can override to show
 *   account-type-specific info.
 * - Throw a custom exception (e.g., InsufficientFundsException) instead
 *   of just printing an error.
 *
 * Compile:  javac BankAccountPractice.java
 * Run:      java BankAccountPractice
 */

public class BankAccountPractice {

    static class Account {
        private String accountNumber;
        private String ownerName;
        private double balance;

        public Account(String accountNumber, String ownerName, double balance) {
            this.accountNumber = accountNumber;
            this.ownerName = ownerName;
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void deposit(double amount) {
            // TODO: validate amount > 0, then add to balance
        }

        public void withdraw(double amount) {
            // TODO: base implementation - block if amount > balance
        }

        // Subclasses need to be able to read/modify balance directly.
        // TODO: decide - add a protected setter, or keep withdraw/deposit
        // as the only way subclasses touch balance (better encapsulation).
        protected void setBalance(double balance) {
            this.balance = balance;
        }
    }

    static class SavingsAccount extends Account {
        private double interestRate;
        private static final double MIN_BALANCE = 500;

        public SavingsAccount(String accountNumber, String ownerName,
                               double balance, double interestRate) {
            super(accountNumber, ownerName, balance);
            this.interestRate = interestRate;
        }

        public void applyInterest() {
            // TODO: implement - balance += balance * interestRate
        }

        @Override
        public void withdraw(double amount) {
            // TODO: implement - block withdrawal if it would drop
            // balance below MIN_BALANCE
        }
    }

    static class CurrentAccount extends Account {
        private double overdraftLimit;

        public CurrentAccount(String accountNumber, String ownerName,
                               double balance, double overdraftLimit) {
            super(accountNumber, ownerName, balance);
            this.overdraftLimit = overdraftLimit;
        }

        @Override
        public void withdraw(double amount) {
            // TODO: implement - allow balance to go negative but not
            // past -overdraftLimit
        }
    }

    public static void main(String[] args) {
        Account savings = new SavingsAccount("SB001", "Asha", 2000, 0.04);
        Account current = new CurrentAccount("CA001", "Rahul", 500, 1000);

        savings.deposit(1000);
        savings.withdraw(2600); // should be blocked (below min balance)

        current.withdraw(1200); // should succeed (within overdraft)
        current.withdraw(500);  // should be blocked (past overdraft)

        System.out.println(savings.getOwnerName() + " balance: " + savings.getBalance());
        System.out.println(current.getOwnerName() + " balance: " + current.getBalance());
    }
}
