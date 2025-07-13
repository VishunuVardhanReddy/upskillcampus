import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accountNumber;
    private String name;
    private String address;
    private String phone;
    private double balance;
    private String password;
    private List<Transaction> transactions;

    // Constructor with password hashing
    public BankAccount(String accountNumber, String name, String address, String phone, double balance, String password) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.balance = balance;
        this.password = hashPassword(password);  // Changed this line
        this.transactions = new ArrayList<>();
        this.addTransaction("Account opened", balance, balance);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getBalance() {
        return balance;
    }

    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(hashPassword(inputPassword));
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.addTransaction("Deposit", amount, this.balance);
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.addTransaction("Withdrawal", -amount, this.balance);
            return true;
        }
        return false;
    }

    public void addTransaction(String description, double amount, double balance) {
        Transaction transaction = new Transaction(description, amount, balance);
        this.transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return "Account Number: " + accountNumber + "\n" +
               "Name: " + name + "\n" +
               "Address: " + address + "\n" +
               "Phone: " + phone + "\n" +
               "Balance: " + balance;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
}

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String date;
    private String description;
    private double amount;
    private double balance;

    public Transaction(String description, double amount, double balance) {
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.description = description;
        this.amount = amount;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("%-20s %-15s %12.2f %12.2f", date, description, amount, balance);
    }
}

class BankSystem {
    private Map<String, BankAccount> accounts;
    private static final String DATA_FILE = System.getProperty("user.home") + "/bank_data.dat";

    public BankSystem() {
        this.accounts = new HashMap<>();
        loadData();
    }

    public void registerAccount(String name, String address, String phone, double initialDeposit, String password) {
        String accountNumber = generateAccountNumber();
        BankAccount account = new BankAccount(accountNumber, name, address, phone, initialDeposit, password);
        accounts.put(accountNumber, account);
        saveData();
        System.out.println("\nRegistration successful!");
        System.out.println("Your account number is: " + accountNumber);
    }

    public BankAccount login(String accountNumber, String password) {
        BankAccount account = accounts.get(accountNumber);
        if (account != null && account.verifyPassword(password)) {
            return account;
        }
        return null;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("%08d", random.nextInt(100000000));
    }

    public boolean transferFunds(String fromAccount, String toAccount, double amount) {
        BankAccount sender = accounts.get(fromAccount);
        BankAccount receiver = accounts.get(toAccount);

        if (sender == null || receiver == null) {
            return false;
        }

        if (sender.withdraw(amount)) {
            receiver.deposit(amount);
            sender.addTransaction("Transfer to " + toAccount, -amount, sender.getBalance());
            receiver.addTransaction("Transfer from " + fromAccount, amount, receiver.getBalance());
            saveData();
            return true;
        }
        return false;
    }

    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No existing data file found. Starting fresh.");
            return;
        }
    
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            accounts = (Map<String, BankAccount>) ois.readObject();
            System.out.println("Successfully loaded " + accounts.size() + " accounts");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
            // Create new empty accounts map if loading fails
            accounts = new HashMap<>();
        }
    }  

    public void saveData() {
        try {
            // First write to temporary file
            File tempFile = new File(DATA_FILE + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(accounts);
            }
        
            // Only replace original if write succeeded
            File originalFile = new File(DATA_FILE);
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IOException("Could not delete old data file");
                }
            }  
            if (!tempFile.renameTo(originalFile)) {
                throw new IOException("Could not rename temp file");
            }
        } 
        catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
            // You might want to add additional error handling here
        }
    }
}

public class BankingInformationSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static BankSystem bankSystem = new BankSystem();
    private static BankAccount currentAccount = null;

    public static void main(String[] args) {
        showMainMenu();
    }

    private static String getHiddenInput(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        }
        // Fallback for IDEs that don't provide a console
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n===== Banking Information System =====");
            System.out.println("1. Register New Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 3);

            switch (choice) {
                case 1:
                    registerAccount();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("Thank you for using our banking system. Goodbye!");
                    System.exit(0);
            }
        }
    }

    private static void registerAccount() {
        System.out.println("\n===== New Account Registration =====");
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your address: ");
        String address = scanner.nextLine();

        System.out.print("Enter your phone number: ");
        String phone = scanner.nextLine();

        System.out.print("Enter initial deposit amount: ");
        double initialDeposit = getDoubleInput(0, Double.MAX_VALUE);

        System.out.print("Set your password: ");
        String password = getHiddenInput("");

        bankSystem.registerAccount(name, address, phone, initialDeposit, password);
    }

    private static void login() {
        System.out.println("\n===== Login =====");
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = getHiddenInput("");

        currentAccount = bankSystem.login(accountNumber, password);

        if (currentAccount != null) {
            System.out.println("\nLogin successful! Welcome, " + currentAccount.getName() + "!");
            showAccountMenu();
        } else {
            System.out.println("Invalid account number or password. Please try again.");
        }
    }

    private static void showAccountMenu() {
        while (currentAccount != null) {
            System.out.println("\n===== Account Menu =====");
            System.out.println("1. View Account Information");
            System.out.println("2. Update Account Information");
            System.out.println("3. Change Password");
            System.out.println("4. Deposit");
            System.out.println("5. Withdraw");
            System.out.println("6. Transfer Funds");
            System.out.println("7. View Account Statement");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(1, 8);

            switch (choice) {
                case 1:
                    viewAccountInfo();
                    break;
                case 2:
                    updateAccountInfo();
                    break;
                case 3:
                    changePassword();
                    break;
                case 4:
                    deposit();
                    break;
                case 5:
                    withdraw();
                    break;
                case 6:
                    transferFunds();
                    break;
                case 7:
                    viewAccountStatement();
                    break;
                case 8:
                    currentAccount = null;
                    System.out.println("Logged out successfully.");
                    return;
            }
        }
    }

    private static void viewAccountInfo() {
        System.out.println("\n===== Account Information =====");
        System.out.println(currentAccount);
    }

    private static void updateAccountInfo() {
        System.out.println("\n===== Update Account Information =====");
        System.out.println("Current Information:");
        System.out.println(currentAccount);

        System.out.print("\nEnter new name (leave blank to keep current): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            currentAccount.setName(name);
        }

        System.out.print("Enter new address (leave blank to keep current): ");
        String address = scanner.nextLine();
        if (!address.isEmpty()) {
            currentAccount.setAddress(address);
        }

        System.out.print("Enter new phone number (leave blank to keep current): ");
        String phone = scanner.nextLine();
        if (!phone.isEmpty()) {
            currentAccount.setPhone(phone);
        }

        bankSystem.saveData();
        System.out.println("\nAccount information updated successfully!");
    }

    private static void changePassword() {
        System.out.println("\n===== Change Password =====");

        System.out.print("Enter current password: ");
        String currentPassword = getHiddenInput("");

        if (currentAccount.verifyPassword(currentPassword)) {
            System.out.print("Enter new password: ");
            String newPassword = getHiddenInput("");
            currentAccount.changePassword(newPassword);
            bankSystem.saveData();
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Incorrect current password. Password not changed.");
        }
    }

    private static void deposit() {
        System.out.println("\n===== Deposit =====");
        System.out.print("Enter amount to deposit: ");
        double amount = getDoubleInput(0.01, Double.MAX_VALUE);

        currentAccount.deposit(amount);
        bankSystem.saveData();
        System.out.printf("Deposit of %.2f successful. New balance: %.2f\n", amount, currentAccount.getBalance());
    }

    private static void withdraw() {
        System.out.println("\n===== Withdrawal =====");
        System.out.print("Enter amount to withdraw: ");
        double amount = getDoubleInput(0.01, currentAccount.getBalance());

        if (currentAccount.withdraw(amount)) {
            bankSystem.saveData();
            System.out.printf("Withdrawal of %.2f successful. New balance: %.2f\n", amount, currentAccount.getBalance());
        } else {
            System.out.println("Withdrawal failed. Insufficient funds.");
        }
    }

    private static void transferFunds() {
        System.out.println("\n===== Fund Transfer =====");
        System.out.print("Enter recipient's account number: ");
        String toAccount = scanner.nextLine();

        if (!bankSystem.accountExists(toAccount)) {
            System.out.println("Recipient account not found.");
            return;
        }

        if (toAccount.equals(currentAccount.getAccountNumber())) {
            System.out.println("Cannot transfer to your own account.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = getDoubleInput(0.01, currentAccount.getBalance());

        if (bankSystem.transferFunds(currentAccount.getAccountNumber(), toAccount, amount)) {
            System.out.printf("Transfer of %.2f to account %s successful.\n", amount, toAccount);
            System.out.printf("New balance: %.2f\n", currentAccount.getBalance());
        } else {
            System.out.println("Transfer failed. Please check your balance and try again.");
        }
    }

    private static void viewAccountStatement() {
        System.out.println("\n===== Account Statement =====");
        System.out.printf("%-20s %-15s %12s %12s\n", "Date", "Description", "Amount", "Balance");
        System.out.println("------------------------------------------------------------");
        
        for (Transaction t : currentAccount.getTransactions()) {
            System.out.println(t);
        }
        
        System.out.println("------------------------------------------------------------");
        System.out.printf("Current Balance: %.2f\n", currentAccount.getBalance());
    }

    private static int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static double getDoubleInput(double min, double max) {
        while (true) {
            try {
                double input = Double.parseDouble(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter an amount between %.2f and %.2f: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}