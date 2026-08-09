import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 1. Interface
interface Exportable {
    String generateCSV();
}

// 2. Abstract Class (Demonstrating Abstraction & Encapsulation)
abstract class Transaction implements Exportable {
    private double amount;     // Private field for encapsulation
    private LocalDate date;    // Private field for encapsulation
    private String description;

    public Transaction(double amount, LocalDate date, String description) {
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Abstract method to be overridden (Polymorphism)
    public abstract double calculateImpact();
}

// 3. Subclass: Income
class Income extends Transaction {
    public Income(double amount, LocalDate date, String description) {
        super(amount, date, description);
    }

    @Override
    public double calculateImpact() {
        return getAmount(); // Adds to total
    }

    @Override
    public String generateCSV() {
        return "Income," + getAmount() + "," + getDate() + "," + getDescription();
    }
}

// 4. Subclass: Expense
class Expense extends Transaction {
    public Expense(double amount, LocalDate date, String description) {
        super(amount, date, description);
    }

    @Override
    public double calculateImpact() {
        return -getAmount(); // Subtracts from total
    }

    @Override
    public String generateCSV() {
        return "Expense," + getAmount() + "," + getDate() + "," + getDescription();
    }
}

// 5. Main Java Swing Application Frame
public class PersonalExpenseTrackerApp extends JFrame {
    private List<Transaction> transactions;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel balanceLabel;
    
    private JTextField amountField;
    private JTextField descField;
    private JComboBox<String> typeComboBox;

    public PersonalExpenseTrackerApp() {
        transactions = new ArrayList<>();

        setTitle("Personal Expense Tracker");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Inputs & Add Button
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Transaction"));

        inputPanel.add(new JLabel("Type:"));
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(new JLabel("Action:"));

        typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        amountField = new JTextField();
        descField = new JTextField();
        JButton addButton = new JButton("Add Transaction");

        inputPanel.add(typeComboBox);
        inputPanel.add(amountField);
        inputPanel.add(descField);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        // Center Panel: JTable to display list of transactions
        tableModel = new DefaultTableModel(new String[]{"Type", "Amount", "Date", "Description"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Panel: Balance & Export to CSV
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        balanceLabel = new JLabel("Current Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton exportButton = new JButton("Export to CSV");

        bottomPanel.add(balanceLabel);
        bottomPanel.add(exportButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Handling for Add Button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });

        // Event Handling for Export Button
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });
    }

    private void addTransaction() {
        try {
            String type = (String) typeComboBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String description = descField.getText().trim();
            LocalDate date = LocalDate.now();

            Transaction transaction;
            if ("Income".equals(type)) {
                transaction = new Income(amount, date, description);
            } else {
                transaction = new Expense(amount, date, description);
            }

            transactions.add(transaction);
            
            // Update Table
            tableModel.addRow(new Object[]{type, amount, date, description});
            
            // Update Balance Display using polymorphism (calculateImpact)
            updateBalance();

            // Clear input fields
            amountField.setText("");
            descField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBalance() {
        double totalBalance = 0;
        for (Transaction t : transactions) {
            totalBalance += t.calculateImpact(); // Polymorphic call
        }
        balanceLabel.setText(String.format("Current Balance: $%.2f", totalBalance));
    }

    private void exportToCSV() {
        try (FileWriter writer = new FileWriter("transactions.csv")) {
            writer.write("Type,Amount,Date,Description\n");
            for (Transaction t : transactions) {
                writer.write(t.generateCSV() + "\n"); // Interface method implementation
            }
            JOptionPane.showMessageDialog(this, "Data successfully exported to transactions.csv!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PersonalExpenseTrackerApp().setVisible(true);
            }
        });
    }
}