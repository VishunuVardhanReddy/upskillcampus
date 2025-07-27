import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.nio.charset.StandardCharsets;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accountNumber;
    private String name;
    private String address;
    private String phone;
    private double balance;
    private String password;
    private List<Transaction> transactions;

    public BankAccount(String accountNumber, String name, String address, String phone, double balance, String password) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.balance = balance;
        this.password = hashPassword(password);
        this.transactions = new ArrayList<>();
        this.addTransaction("Account opened", balance, balance);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public double getBalance() { return balance; }

    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(hashPassword(inputPassword));
    }

    public void changePassword(String newPassword) {
        this.password = hashPassword(newPassword);
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
        transactions.add(new Transaction(description, amount, balance));
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return String.format("Account Number: %s\nName: %s\nAddress: %s\nPhone: %s\nBalance: Rs.%,.2f",
                accountNumber, name, address, phone, balance);
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

    public String getDate() { return date; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public double getBalance() { return balance; }

    @Override
    public String toString() {
        return String.format("%-20s %-15s Rs.%,12.2f Rs.%,12.2f", date, description, amount, balance);
    }
}

class BankSystem {
    private Map<String, BankAccount> accounts;
    private static final String DATA_FILE = System.getProperty("user.home") + "/bank_data.dat";

    public BankSystem() {
        this.accounts = new HashMap<>();
        loadData();
    }

    public String registerAccount(String name, String address, String phone, double initialDeposit, String password) {
        String accountNumber = generateAccountNumber();
        accounts.put(accountNumber, new BankAccount(accountNumber, name, address, phone, initialDeposit, password));
        saveData();
        return accountNumber;
    }

    public BankAccount login(String accountNumber, String password) {
        BankAccount account = accounts.get(accountNumber);
        return (account != null && account.verifyPassword(password)) ? account : null;
    }

    public boolean transferFunds(String fromAccount, String toAccount, double amount) {
        BankAccount sender = accounts.get(fromAccount);
        BankAccount receiver = accounts.get(toAccount);

        if (sender != null && receiver != null && sender.withdraw(amount)) {
            receiver.deposit(amount);
            saveData();
            return true;
        }
        return false;
    }

    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    public String generateAccountNumber() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            accounts = (Map<String, BankAccount>) ois.readObject();
        } catch (Exception e) {
            accounts = new HashMap<>();
        }
    }

    public void saveData() {
        try {
            File tempFile = new File(DATA_FILE + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(accounts);
            }
            new File(DATA_FILE).delete();
            tempFile.renameTo(new File(DATA_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class ModernBankingApp extends JFrame {
    private BankSystem bankSystem = new BankSystem();
    private BankAccount currentAccount;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);
    
    // Enhanced professional color scheme
    private final Color PRIMARY_COLOR = new Color(25, 118, 210);        // Material Blue
    private final Color PRIMARY_DARK = new Color(13, 71, 161);          // Darker Blue
    private final Color SECONDARY_COLOR = new Color(248, 249, 250);     // Light Gray
    private final Color ACCENT_COLOR = new Color(0, 150, 136);          // Teal
    private final Color ACCENT_LIGHT = new Color(178, 235, 242);        // Light Teal
    private final Color SUCCESS_COLOR = new Color(76, 175, 80);         // Green
    private final Color WARNING_COLOR = new Color(244, 67, 54);         // Red
    private final Color TEXT_PRIMARY = new Color(33, 33, 33);           // Dark Gray
    private final Color TEXT_SECONDARY = new Color(117, 117, 117);      // Medium Gray
    private final Color CARD_BACKGROUND = new Color(255, 255, 255);     // Pure White
    private final Color SURFACE_COLOR = new Color(250, 250, 250);       // Off White
    private final Color DIVIDER_COLOR = new Color(224, 224, 224);       // Light Divider
    
    // Professional typography - use fully qualified java.awt.Font
    private final java.awt.Font DISPLAY_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32);
    private final java.awt.Font HEADLINE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24);
    private final java.awt.Font TITLE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20);
    private final java.awt.Font SUBTITLE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16);
    private final java.awt.Font BODY_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
    private final java.awt.Font CAPTION_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
    private final java.awt.Font BUTTON_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
    private final java.awt.Font BALANCE_FONT = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 36);
    
    // UI Components
    private LoginPanel loginPanel;
    private MainMenuPanel mainMenuPanel;
    private AccountInfoPanel accountInfoPanel;
    private TransactionPanel transactionPanel;
    private StatementPanel statementPanel;
    private PasswordChangePanel passwordChangePanel;
    private UpdateInfoPanel updateInfoPanel;

    public ModernBankingApp() {
        configureFrame();
        initializePanels();
        showLoginScreen();
    }

    private void configureFrame() {
        setTitle("SecureBank Pro - Professional Banking System");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set application icon
        try {
            setIconImage(createBankIcon());
        } catch (Exception e) {
            // Icon creation failed, continue without icon
        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Customize UI defaults
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("ScrollBar.thumb", ACCENT_COLOR);
            UIManager.put("ScrollBar.track", SURFACE_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Enable anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Professional gradient background
        cardPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create sophisticated gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(240, 248, 255),
                    getWidth(), getHeight(), new Color(230, 245, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle pattern
                g2d.setColor(new Color(255, 255, 255, 30));
                for (int x = 0; x < getWidth(); x += 50) {
                    for (int y = 0; y < getHeight(); y += 50) {
                        g2d.fillOval(x, y, 2, 2);
                    }
                }
            }
        };
        add(cardPanel);
    }

    private java.awt.Image createBankIcon() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Create a simple bank icon
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillRect(4, 20, 24, 8);
        g2d.fillRect(8, 8, 4, 12);
        g2d.fillRect(12, 8, 4, 12);
        g2d.fillRect(16, 8, 4, 12);
        g2d.fillRect(20, 8, 4, 12);
        g2d.fillRect(6, 4, 20, 4);
        
        g2d.dispose();
        return icon;
    }

    private void initializePanels() {
        loginPanel = new LoginPanel();
        mainMenuPanel = new MainMenuPanel();
        accountInfoPanel = new AccountInfoPanel();
        transactionPanel = new TransactionPanel();
        statementPanel = new StatementPanel();
        passwordChangePanel = new PasswordChangePanel();
        updateInfoPanel = new UpdateInfoPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(mainMenuPanel, "MAIN_MENU");
        cardPanel.add(accountInfoPanel, "ACCOUNT_INFO");
        cardPanel.add(transactionPanel, "TRANSACTION");
        cardPanel.add(statementPanel, "STATEMENT");
        cardPanel.add(passwordChangePanel, "PASSWORD_CHANGE");
        cardPanel.add(updateInfoPanel, "UPDATE_INFO");
    }

    private void showLoginScreen() {
        cardLayout.show(cardPanel, "LOGIN");
        loginPanel.clearFields();
    }

    private void showMainMenu() {
        mainMenuPanel.updateWelcomeMessage();
        cardLayout.show(cardPanel, "MAIN_MENU");
    }

    // Utility method for consistent currency formatting
    private String formatCurrency(double amount) {
        return "Rs." + String.format("%,.2f", amount);
    }

    protected JButton createModernButton(String text, Color bgColor, ActionListener action) {
        JButton button = new JButton(text) {
            private boolean hovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                Color startColor = hovered ? bgColor.brighter() : bgColor;
                Color endColor = hovered ? bgColor : bgColor.darker();
                
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Add subtle shadow when hovered
                if (hovered) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 12, 12);
                }
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.addActionListener(action);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 45));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("hovered", true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("hovered", false);
                button.repaint();
            }
        });
        
        return button;
    }

    protected JPanel createCardPanel(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card shadow
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 16, 16);
                
                // Card background
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setLayout(new BorderLayout(15, 15));
        
        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_PRIMARY);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            card.add(titleLabel, BorderLayout.NORTH);
        }
        
        return card;
    }

    class LoginPanel extends JPanel {
        private JTextField accountField = new JTextField(20);
        private JPasswordField passwordField = new JPasswordField(20);

        public LoginPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(25, 25, 25, 25);

            // Professional header with logo area
            JPanel headerPanel = new JPanel();
            headerPanel.setOpaque(false);
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            
            JLabel bankIcon = new JLabel("[BANK]");
            bankIcon.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
            bankIcon.setForeground(PRIMARY_COLOR);
            bankIcon.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel header = new JLabel("SecureBank Pro");
            header.setFont(DISPLAY_FONT);
            header.setForeground(PRIMARY_COLOR);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel subtitle = new JLabel("Professional Banking System");
            subtitle.setFont(BODY_FONT);
            subtitle.setForeground(TEXT_SECONDARY);
            subtitle.setHorizontalAlignment(SwingConstants.CENTER);
            
            headerPanel.add(bankIcon);
            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(header);
            headerPanel.add(Box.createVerticalStrut(5));
            headerPanel.add(subtitle);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(headerPanel, gbc);

            // Enhanced login form
            JPanel formPanel = createCardPanel(null);
            formPanel.setLayout(new GridBagLayout());
            GridBagConstraints fgbc = new GridBagConstraints();
            fgbc.insets = new Insets(15, 15, 15, 15);
            
            JLabel formTitle = new JLabel("Sign In to Your Account");
            formTitle.setFont(SUBTITLE_FONT);
            formTitle.setForeground(TEXT_PRIMARY);
            formTitle.setHorizontalAlignment(SwingConstants.CENTER);
            fgbc.gridx = 0;
            fgbc.gridy = 0;
            fgbc.gridwidth = 2;
            formPanel.add(formTitle, fgbc);
            
            // Account number field
            JLabel accountLabel = new JLabel("Account Number");
            accountLabel.setFont(BODY_FONT);
            accountLabel.setForeground(TEXT_PRIMARY);
            fgbc.gridx = 0;
            fgbc.gridy = 1;
            fgbc.gridwidth = 2;
            fgbc.anchor = GridBagConstraints.WEST;
            formPanel.add(accountLabel, fgbc);
            
            accountField.setFont(BODY_FONT);
            accountField.setBorder(createModernInputBorder());
            accountField.setPreferredSize(new Dimension(300, 45));
            accountField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        attemptLogin();
                    }
                }
            });
            fgbc.gridy = 2;
            fgbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(accountField, fgbc);
            
            // Password field
            JLabel passwordLabel = new JLabel("Password");
            passwordLabel.setFont(BODY_FONT);
            passwordLabel.setForeground(TEXT_PRIMARY);
            fgbc.gridy = 3;
            fgbc.fill = GridBagConstraints.NONE;
            formPanel.add(passwordLabel, fgbc);
            
            passwordField.setFont(BODY_FONT);
            passwordField.setBorder(createModernInputBorder());
            passwordField.setPreferredSize(new Dimension(300, 45));
            passwordField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        attemptLogin();
                    }
                }
            });
            fgbc.gridy = 4;
            fgbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(passwordField, fgbc);

            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(formPanel, gbc);

            // Enhanced buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            buttonPanel.setOpaque(false);
            
            JButton loginButton = createModernButton("SIGN IN", PRIMARY_COLOR, e -> attemptLogin());
            JButton registerButton = createModernButton("REGISTER", ACCENT_COLOR, e -> showRegistrationDialog());
            
            buttonPanel.add(loginButton);
            buttonPanel.add(registerButton);

            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(buttonPanel, gbc);
        }

        private Border createModernInputBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            );
        }
        
        private void attemptLogin() {
            currentAccount = bankSystem.login(accountField.getText(), new String(passwordField.getPassword()));
            if (currentAccount != null) {
                showMainMenu();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='color:#d32f2f; font-size:12pt;'>Invalid account number or password</div></html>", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showRegistrationDialog() {
            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            panel.setBackground(Color.WHITE);
            
            JTextField nameField = new JTextField();
            JTextField addressField = new JTextField();
            JTextField phoneField = new JTextField();
            JTextField depositField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPasswordField confirmField = new JPasswordField();

            Component[] fields = {nameField, addressField, phoneField, depositField, passwordField, confirmField};
            for (Component field : fields) {
                field.setFont(BODY_FONT);
                if (field instanceof JTextField) {
                    ((JTextField)field).setBorder(createModernInputBorder());
                } else {
                    ((JPasswordField)field).setBorder(createModernInputBorder());
                }
            }

            String[] labels = {"Name:", "Address:", "Phone:", "Initial Deposit:", "Password:", "Confirm Password:"};
            for (int i = 0; i < labels.length; i++) {
                JLabel label = new JLabel(labels[i]);
                label.setFont(BODY_FONT);
                label.setForeground(TEXT_PRIMARY);
                panel.add(label);
                panel.add(fields[i]);
            }

            int result = JOptionPane.showConfirmDialog(this, panel, "Register New Account", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                if (!new String(passwordField.getPassword()).equals(new String(confirmField.getPassword()))) {
                    JOptionPane.showMessageDialog(this, 
                        "<html><div style='color:#d32f2f;'>Passwords don't match!</div></html>", 
                        "Registration Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double deposit = Double.parseDouble(depositField.getText());
                    if (deposit < 0) throw new NumberFormatException();
                    
                    String accountNumber = bankSystem.registerAccount(
                        nameField.getText(),
                        addressField.getText(),
                        phoneField.getText(),
                        deposit,
                        new String(passwordField.getPassword())
                    );
                    
                    JOptionPane.showMessageDialog(this, 
                        "<html><div style='font-size:12pt;'>Registration successful!<br>Your account number is: <b>" + 
                        accountNumber + "</b></div></html>", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, 
                        "<html><div style='color:#d32f2f;'>Invalid deposit amount!</div></html>", 
                        "Registration Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void clearFields() {
            accountField.setText("");
            passwordField.setText("");
        }
    }

    class MainMenuPanel extends JPanel {
        private JLabel welcomeLabel = new JLabel();
        private JLabel balanceLabel = new JLabel("", JLabel.CENTER);
        private JLabel accountLabel = new JLabel("", JLabel.CENTER);

        public MainMenuPanel() {
            setOpaque(false);
            setLayout(new BorderLayout(30, 30));
            setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            // Professional header with account info
            JPanel headerPanel = createCardPanel(null);
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            
            welcomeLabel.setFont(HEADLINE_FONT);
            welcomeLabel.setForeground(TEXT_PRIMARY);
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            balanceLabel.setFont(BALANCE_FONT);
            balanceLabel.setForeground(SUCCESS_COLOR);
            balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            accountLabel.setFont(BODY_FONT);
            accountLabel.setForeground(TEXT_SECONDARY);
            accountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            headerPanel.add(welcomeLabel);
            headerPanel.add(Box.createVerticalStrut(15));
            headerPanel.add(balanceLabel);
            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(accountLabel);

            // Quick actions with icons
            JPanel quickActionsPanel = createCardPanel("Quick Actions");
            quickActionsPanel.setLayout(new GridLayout(1, 4, 15, 15));
            
            String[] quickActions = {"[+] Deposit", "[>] Transfer", "[=] Statement", "[X] Logout"};
            Color[] quickColors = {SUCCESS_COLOR, PRIMARY_COLOR, ACCENT_COLOR, WARNING_COLOR};
            
            for (int i = 0; i < quickActions.length; i++) {
                String action = quickActions[i].split(" ")[1];
                String icon = quickActions[i].split(" ")[0];
                Color color = quickColors[i];
                
                JButton btn = createModernButton(icon + " " + action, color, e -> {
                    switch (action) {
                        case "Deposit":
                            transactionPanel.setTransactionType("Deposit");
                            cardLayout.show(cardPanel, "TRANSACTION");
                            break;
                        case "Transfer":
                            transactionPanel.setTransactionType("Transfer");
                            cardLayout.show(cardPanel, "TRANSACTION");
                            break;
                        case "Statement":
                            statementPanel.updateStatement();
                            cardLayout.show(cardPanel, "STATEMENT");
                            break;
                        case "Logout":
                            currentAccount = null;
                            showLoginScreen();
                            break;
                    }
                });
                quickActionsPanel.add(btn);
            }

            // Main menu grid with enhanced buttons
            JPanel menuGrid = new JPanel(new GridLayout(2, 3, 20, 20));
            menuGrid.setOpaque(false);
            
            String[] menuItems = {
                "[i] Account Overview", "[+] Deposit Funds", "[-] Withdraw Funds",
                "[>] Transfer Money", "[=] View Statement", "[*] Change Password"
            };
            
            Color[] menuColors = {
                PRIMARY_COLOR, SUCCESS_COLOR, WARNING_COLOR,
                ACCENT_COLOR, PRIMARY_DARK, TEXT_PRIMARY
            };
            
            Runnable[] menuActions = {
                () -> { accountInfoPanel.updateInfo(); cardLayout.show(cardPanel, "ACCOUNT_INFO"); },
                () -> { transactionPanel.setTransactionType("Deposit"); cardLayout.show(cardPanel, "TRANSACTION"); },
                () -> { transactionPanel.setTransactionType("Withdraw"); cardLayout.show(cardPanel, "TRANSACTION"); },
                () -> { transactionPanel.setTransactionType("Transfer"); cardLayout.show(cardPanel, "TRANSACTION"); },
                () -> { statementPanel.updateStatement(); cardLayout.show(cardPanel, "STATEMENT"); },
                () -> { passwordChangePanel.clearFields(); cardLayout.show(cardPanel, "PASSWORD_CHANGE"); }
            };
            
            for (int i = 0; i < menuItems.length; i++) {
                final int index = i;
                JButton button = createModernButton(menuItems[i], menuColors[i], 
                    e -> menuActions[index].run());
                button.setPreferredSize(new Dimension(200, 60));
                menuGrid.add(button);
            }

            // Layout assembly
            JPanel topSection = new JPanel(new BorderLayout(20, 20));
            topSection.setOpaque(false);
            topSection.add(headerPanel, BorderLayout.NORTH);
            topSection.add(quickActionsPanel, BorderLayout.SOUTH);
            
            add(topSection, BorderLayout.NORTH);
            add(menuGrid, BorderLayout.CENTER);
        }

        public void updateWelcomeMessage() {
            if (currentAccount != null) {
                welcomeLabel.setText("Welcome back, " + currentAccount.getName());
                balanceLabel.setText(formatCurrency(currentAccount.getBalance()));
                accountLabel.setText("Account #" + currentAccount.getAccountNumber());
            }
        }
    }

    class AccountInfoPanel extends JPanel {
        private JTextArea infoArea = new JTextArea(10, 30);

        public AccountInfoPanel() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));
            
            JLabel header = new JLabel("Account Information");
            header.setFont(HEADLINE_FONT);
            header.setForeground(PRIMARY_COLOR);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            add(header, BorderLayout.NORTH);
            
            infoArea.setEditable(false);
            infoArea.setFont(BODY_FONT);
            infoArea.setBackground(Color.WHITE);
            infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            JScrollPane scrollPane = new JScrollPane(infoArea);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            add(scrollPane, BorderLayout.CENTER);
            
            JButton backButton = createModernButton("Back to Main Menu", PRIMARY_COLOR, 
                e -> showMainMenu());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.add(backButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public void updateInfo() {
            infoArea.setText(currentAccount.toString());
        }
    }

    class TransactionPanel extends JPanel {
        private String transactionType;
        private JLabel typeLabel = new JLabel("", JLabel.CENTER);
        private JTextField amountField = new JTextField(15);
        private JTextField accountField = new JTextField(15);

        public TransactionPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            typeLabel.setFont(HEADLINE_FONT);
            typeLabel.setForeground(PRIMARY_COLOR);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(typeLabel, gbc);

            JPanel formPanel = createCardPanel(null);
            formPanel.setLayout(new GridBagLayout());
            GridBagConstraints fgbc = new GridBagConstraints();
            fgbc.insets = new Insets(5, 5, 5, 5);
            fgbc.anchor = GridBagConstraints.WEST;
            
            JLabel amountLabel = new JLabel("Amount:");
            amountLabel.setFont(BODY_FONT);
            fgbc.gridx = 0;
            fgbc.gridy = 0;
            formPanel.add(amountLabel, fgbc);
            
            amountField.setFont(BODY_FONT);
            amountField.setBorder(createInputBorder());
            amountField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        processTransaction();
                    }
                }
            });
            fgbc.gridx = 1;
            formPanel.add(amountField, fgbc);
            
            JLabel accountLabel = new JLabel("To Account:");
            accountLabel.setFont(BODY_FONT);
            fgbc.gridx = 0;
            fgbc.gridy = 1;
            formPanel.add(accountLabel, fgbc);
            
            accountField.setFont(BODY_FONT);
            accountField.setBorder(createInputBorder());
            accountField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        processTransaction();
                    }
                }
            });
            fgbc.gridx = 1;
            formPanel.add(accountField, fgbc);
            accountLabel.setVisible(false);
            accountField.setVisible(false);
            
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            add(formPanel, gbc);

            JButton submitButton = createModernButton("Submit", ACCENT_COLOR, 
                e -> processTransaction());
            
            JButton backButton = createModernButton("Back to Main Menu", PRIMARY_COLOR, 
                e -> showMainMenu());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setOpaque(false);
            buttonPanel.add(submitButton);
            buttonPanel.add(backButton);
            
            gbc.gridy = 2;
            add(buttonPanel, gbc);
        }

        private Border createInputBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            );
        }

        public void setTransactionType(String type) {
            this.transactionType = type;
            typeLabel.setText(type);
            amountField.setText("");
            accountField.setText("");
            
            boolean showTransferFields = type.equals("Transfer");
            Component[] components = amountField.getParent().getComponents();
            for (Component c : components) {
                if (c instanceof JLabel && ((JLabel)c).getText().equals("To Account:")) {
                    c.setVisible(showTransferFields);
                }
                if (c instanceof JTextField && c != amountField) {
                    c.setVisible(showTransferFields);
                }
            }
        }

        private void processTransaction() {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) throw new NumberFormatException();

                switch (transactionType) {
                    case "Deposit":
                        currentAccount.deposit(amount);
                        JOptionPane.showMessageDialog(this, 
                            "<html><div style='font-size:12pt;'>Deposit of " + formatCurrency(amount) + 
                            " successful!<br>New balance: " + formatCurrency(currentAccount.getBalance()) + "</div></html>", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case "Withdraw":
                        if (currentAccount.withdraw(amount)) {
                            JOptionPane.showMessageDialog(this, 
                                "<html><div style='font-size:12pt;'>Withdrawal of " + formatCurrency(amount) + 
                                " successful!<br>New balance: " + formatCurrency(currentAccount.getBalance()) + "</div></html>", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "<html><div style='color:#d32f2f; font-size:12pt;'>Insufficient funds!</div></html>", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        break;
                    case "Transfer":
                        String toAccount = accountField.getText();
                        if (bankSystem.transferFunds(currentAccount.getAccountNumber(), toAccount, amount)) {
                            JOptionPane.showMessageDialog(this, 
                                "<html><div style='font-size:12pt;'>Transfer of " + formatCurrency(amount) + 
                                " to account " + toAccount + " successful!<br>New balance: " + 
                                formatCurrency(currentAccount.getBalance()) + "</div></html>", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "<html><div style='color:#d32f2f; font-size:12pt;'>Transfer failed!<br>Check account number or balance.</div></html>", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        break;
                }
                bankSystem.saveData();
                showMainMenu();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='color:#d32f2f; font-size:12pt;'>Invalid amount!</div></html>", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class StatementPanel extends JPanel {
        private JTextArea statementArea = new JTextArea(20, 60);
        private JButton pdfButton = createModernButton("[v] Download PDF", ACCENT_COLOR, e -> generatePDFStatement());
        private JLabel accountLabel;
        private JLabel balanceLabel;

        public StatementPanel() {
            setOpaque(false);
            setLayout(new BorderLayout(20, 20));
            setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            // Professional header
            JPanel headerPanel = createCardPanel("[=] Account Statement");
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            
            accountLabel = new JLabel();
            accountLabel.setFont(SUBTITLE_FONT);
            accountLabel.setForeground(TEXT_PRIMARY);
            
            balanceLabel = new JLabel();
            balanceLabel.setFont(TITLE_FONT);
            balanceLabel.setForeground(SUCCESS_COLOR);
            
            headerPanel.add(accountLabel);
            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(balanceLabel);
            
            // Enhanced statement area
            statementArea.setEditable(false);
            statementArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            statementArea.setBackground(SURFACE_COLOR);
            statementArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JScrollPane scrollPane = new JScrollPane(statementArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(DIVIDER_COLOR, 1));
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            
            // Custom scrollbar
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = ACCENT_COLOR;
                    this.trackColor = SURFACE_COLOR;
                }
            });
            
            // Action buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(pdfButton);
            buttonPanel.add(createModernButton("[<] Back", PRIMARY_COLOR, e -> showMainMenu()));
            
            add(headerPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        public void updateStatement() {
            if (currentAccount == null) return;
            
            // Update header labels with current account info
            accountLabel.setText("Account #" + currentAccount.getAccountNumber() + " - " + currentAccount.getName());
            balanceLabel.setText("Current Balance: " + formatCurrency(currentAccount.getBalance()));
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-20s %-15s %12s %12s\n", "Date", "Description", "Amount", "Balance"));
            sb.append("------------------------------------------------------------\n");
            
            for (Transaction t : currentAccount.getTransactions()) {
                sb.append(t.toString()).append("\n");
            }
            
            sb.append("------------------------------------------------------------\n");
            sb.append(String.format("%52s %s\n", "Current Balance:", formatCurrency(currentAccount.getBalance())));
            
            statementArea.setText(sb.toString());
        }

        private void generatePDFStatement() {
            if (currentAccount == null) return;
            
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Statement as PDF");
                fileChooser.setSelectedFile(new File("BankStatement_" + currentAccount.getAccountNumber() + ".pdf"));
                
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        file = new File(file.getParentFile(), file.getName() + ".pdf");
                    }
                    
                    // Use a font that supports rupee symbol
                    BaseFont baseFont = BaseFont.createFont(
                        BaseFont.HELVETICA, 
                        BaseFont.CP1252, 
                        BaseFont.EMBEDDED
                    );
                    
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(file));
                    document.open();
                    
                    com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLUE);
                    Paragraph title = new Paragraph("Bank Statement", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    document.add(title);
                    
                    com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(baseFont, 12);
                    document.add(new Paragraph("Account Holder: " + currentAccount.getName(), infoFont));
                    document.add(new Paragraph("Account Number: " + currentAccount.getAccountNumber(), infoFont));
                    document.add(new Paragraph("Statement Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), infoFont));
                    document.add(new Paragraph(" "));
                    
                    PdfPTable table = new PdfPTable(4);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10);
                    table.setSpacingAfter(10);
                    
                    com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD);
                    table.addCell(new Phrase("Date", headerFont));
                    table.addCell(new Phrase("Description", headerFont));
                    table.addCell(new Phrase("Amount", headerFont));
                    table.addCell(new Phrase("Balance", headerFont));
                    
                    com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(baseFont, 12);
                    for (Transaction t : currentAccount.getTransactions()) {
                        table.addCell(new Phrase(t.getDate(), dataFont));
                        table.addCell(new Phrase(t.getDescription(), dataFont));
                        table.addCell(new Phrase(formatCurrency(t.getAmount()), dataFont));
                        table.addCell(new Phrase(formatCurrency(t.getBalance()), dataFont));
                    }
                    
                    document.add(table);
                    
                    com.itextpdf.text.Font balanceFont = new com.itextpdf.text.Font(baseFont, 14, com.itextpdf.text.Font.BOLD);
                    Paragraph balance = new Paragraph("Current Balance: " + 
                        formatCurrency(currentAccount.getBalance()), balanceFont);
                    balance.setAlignment(Element.ALIGN_RIGHT);
                    document.add(balance);
                    
                    document.close();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Statement saved as PDF successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error generating PDF: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class PasswordChangePanel extends JPanel {
        private JPasswordField currentPasswordField = new JPasswordField(15);
        private JPasswordField newPasswordField = new JPasswordField(15);
        private JPasswordField confirmPasswordField = new JPasswordField(15);

        public PasswordChangePanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel header = new JLabel("Change Password");
            header.setFont(HEADLINE_FONT);
            header.setForeground(PRIMARY_COLOR);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(header, gbc);

            JPanel formPanel = createCardPanel(null);
            formPanel.setLayout(new GridLayout(4, 2, 10, 15));
            
            String[] labels = {"Current Password:", "New Password:", "Confirm New Password:"};
            JPasswordField[] fields = {currentPasswordField, newPasswordField, confirmPasswordField};
            
            for (int i = 0; i < labels.length; i++) {
                JLabel label = new JLabel(labels[i]);
                label.setFont(BODY_FONT);
                label.setForeground(TEXT_PRIMARY);
                formPanel.add(label);
                
                fields[i].setFont(BODY_FONT);
                fields[i].setBorder(createInputBorder());
                fields[i].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            changePassword();
                        }
                    }
                });
                formPanel.add(fields[i]);
            }

            gbc.gridy = 1;
            add(formPanel, gbc);

            JButton submitButton = createModernButton("Change Password", ACCENT_COLOR, 
                e -> changePassword());
            
            JButton backButton = createModernButton("Back to Main Menu", PRIMARY_COLOR, 
                e -> showMainMenu());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setOpaque(false);
            buttonPanel.add(submitButton);
            buttonPanel.add(backButton);
            
            gbc.gridy = 2;
            add(buttonPanel, gbc);
        }

        private Border createInputBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            );
        }

        private void changePassword() {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='color:#d32f2f; font-size:12pt;'>New passwords don't match!</div></html>", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (currentAccount.verifyPassword(currentPassword)) {
                currentAccount.changePassword(newPassword);
                bankSystem.saveData();
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='font-size:12pt;'>Password changed successfully!</div></html>", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                showMainMenu();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='color:#d32f2f; font-size:12pt;'>Incorrect current password!</div></html>", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        public void clearFields() {
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }
    }

    class UpdateInfoPanel extends JPanel {
        private JTextField nameField = new JTextField(20);
        private JTextField addressField = new JTextField(20);
        private JTextField phoneField = new JTextField(20);

        public UpdateInfoPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel header = new JLabel("Update Account Information");
            header.setFont(HEADLINE_FONT);
            header.setForeground(PRIMARY_COLOR);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(header, gbc);

            JPanel formPanel = createCardPanel(null);
            formPanel.setLayout(new GridLayout(4, 2, 10, 15));
            
            String[] labels = {"Name:", "Address:", "Phone:"};
            JTextField[] fields = {nameField, addressField, phoneField};
            
            for (int i = 0; i < labels.length; i++) {
                JLabel label = new JLabel(labels[i]);
                label.setFont(BODY_FONT);
                label.setForeground(TEXT_PRIMARY);
                formPanel.add(label);
                
                fields[i].setFont(BODY_FONT);
                fields[i].setBorder(createInputBorder());
                fields[i].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            updateInfo();
                        }
                    }
                });
                formPanel.add(fields[i]);
            }

            gbc.gridy = 1;
            add(formPanel, gbc);

            JButton submitButton = createModernButton("Update Information", ACCENT_COLOR, 
                e -> updateInfo());
            
            JButton backButton = createModernButton("Back to Main Menu", PRIMARY_COLOR, 
                e -> showMainMenu());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setOpaque(false);
            buttonPanel.add(submitButton);
            buttonPanel.add(backButton);
            
            gbc.gridy = 2;
            add(buttonPanel, gbc);
        }

        private Border createInputBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            );
        }

        public void loadCurrentInfo() {
            if (currentAccount != null) {
                nameField.setText(currentAccount.getName());
                addressField.setText(currentAccount.getAddress());
                phoneField.setText(currentAccount.getPhone());
            }
        }

        private void updateInfo() {
            currentAccount.setName(nameField.getText());
            currentAccount.setAddress(addressField.getText());
            currentAccount.setPhone(phoneField.getText());
            bankSystem.saveData();
            JOptionPane.showMessageDialog(this, 
                "<html><div style='font-size:12pt;'>Account information updated successfully!</div></html>", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            showMainMenu();
        }
    }

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.opengl", "true");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Add splash screen effect
                JWindow splash = createSplashScreen();
                splash.setVisible(true);
                
                // Simulate loading time
                javax.swing.Timer timer = new javax.swing.Timer(2500, e -> {
                    splash.dispose();
                    ModernBankingApp app = new ModernBankingApp();
                    app.setVisible(true);
                });
                timer.setRepeats(false);
                timer.start();
                
            } catch (Exception e) {
                // If splash fails, show app directly
                ModernBankingApp app = new ModernBankingApp();
                app.setVisible(true);
            }
        });
    }
    
    private static JWindow createSplashScreen() {
        JWindow splash = new JWindow();
        splash.setSize(400, 250);
        splash.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 118, 210), 
                                                   getWidth(), getHeight(), new Color(13, 71, 161));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel logo = new JLabel("[BANK]");
        logo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title = new JLabel("SecureBank Pro");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Professional Banking System");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        subtitle.setForeground(new Color(255, 255, 255, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setForeground(Color.WHITE);
        progress.setBackground(new Color(255, 255, 255, 50));
        progress.setMaximumSize(new Dimension(200, 6));
        
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.add(Box.createVerticalGlue());
        panel.add(logo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(30));
        panel.add(progress);
        panel.add(Box.createVerticalGlue());
        
        splash.add(panel);
        return splash;
    }
}