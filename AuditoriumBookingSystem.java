import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AuditoriumBookingSystem {
    private static final HashMap<String, String> users = new HashMap<>();
    private static final ArrayList<User> userList = new ArrayList<>();
    private static final ArrayList<Booking> bookings = new ArrayList<>();
    private static Connection conn;

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/auditorium_booking", "****", "******");
            // Initialize default users
            users.put("admin", "admin123");
            users.put("customer", "cust123");

//            userList.add(new User("admin", "admin123", "admin", "N/A"));
//            userList.add(new User("customer", "cust123", "user", "1234567890"));

            // Load bookings from database
            loadBookingsFromDatabase();

            // Start with the calendar view
            showCalendarWindow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //    private static void loadBookingsFromDatabase() throws SQLException {
//        String query = "SELECT * FROM bookings";
//        Statement stmt = conn.createStatement();
//        ResultSet rs = stmt.executeQuery(query);
//        while (rs.next()) {
//            bookings.add(new Booking(
//                    rs.getInt("id"),
//                    rs.getString("date"),
//                    rs.getString("status"),
//                    rs.getString("customer_name"),
//                    rs.getString("phone_number")
//            ));
//        }
//    }
    private static void loadBookingsFromDatabase() throws SQLException {
        bookings.clear(); // Clear the existing list to avoid duplicates
        String query = "SELECT * FROM bookings";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            bookings.add(new Booking(
                    rs.getInt("id"),
                    rs.getString("date"),
                    rs.getString("status"),
                    rs.getString("customer_name"),
                    rs.getString("phone_number")
            ));
        }
    }


    private static void showCalendarWindow() {
        JFrame frame = new JFrame("Auditorium Booking Calendar");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel monthLabel = new JLabel("Enter Month (MM):", SwingConstants.CENTER);
        JTextField monthField = new JTextField(10);
        JButton viewButton = new JButton("View Calendar");
        JButton loginButton = new JButton("Login");
        JButton createAccountButton = new JButton("Create User Account");

        JPanel inputPanel = new JPanel();
        inputPanel.add(monthLabel);
        inputPanel.add(monthField);
        inputPanel.add(viewButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        JPanel calendarPanel = new JPanel(new GridLayout(6, 7));
        panel.add(calendarPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.add(loginButton);
        actionPanel.add(createAccountButton);
        panel.add(actionPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        viewButton.addActionListener(e -> {
            String month = monthField.getText();
            if (month.matches("\\d{2}") && Integer.parseInt(month) >= 1 && Integer.parseInt(month) <= 12) {
                updateCalendarView(calendarPanel, month);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a valid month in MM format (01-12).");
            }
        });

        loginButton.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        createAccountButton.addActionListener(e -> {
            frame.dispose();
            showCreateUserScreen();
        });
    }

    private static void updateCalendarView(JPanel calendarPanel, String month) {
        calendarPanel.removeAll();

        // Get number of days in the month
        int daysInMonth = getDaysInMonth(month);

        for (int day = 1; day <= daysInMonth; day++) {
            String date = "2024-" + month + "-" + String.format("%02d", day);
            JButton dateButton = new JButton(String.valueOf(day));
            dateButton.setOpaque(true);
            dateButton.setBorderPainted(false);

            // Color the button based on booking status
            String status = getBookingStatus(date);
            if (status != null) {
                switch (status) {
                    case "Pending":
                        dateButton.setBackground(Color.YELLOW);
                        break;
                    case "Booked":
                        dateButton.setBackground(Color.RED);
                        break;
                    case "Completed":
                        dateButton.setBackground(Color.BLUE);
                        break;
                }
            } else {
                dateButton.setBackground(Color.GREEN); // Available
            }

            calendarPanel.add(dateButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private static int getDaysInMonth(String month) {
        switch (month) {
            case "02":
                return 28; // February, assuming not a leap year
            case "04":
            case "06":
            case "09":
            case "11":
                return 30; // April, June, September, November
            default:
                return 31; // Other months
        }
    }

    private static String getBookingStatus(String date) {
        for (Booking booking : bookings) {
            if (booking.date.equals(date)) {
                return booking.status;
            }
        }
        return null;
    }

    private static void showLoginScreen() {
        JFrame frame = new JFrame("Login");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(120, 20, 150, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(120, 50, 150, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(20, 90, 250, 25);
        panel.add(loginButton);

        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setBounds(20, 120, 250, 25);
        panel.add(messageLabel);

//        loginButton.addActionListener(e -> {
//            String username = userText.getText();
//            String password = new String(passwordText.getPassword());
//
//            if (users.containsKey(username) && users.get(username).equals(password)) {
//                frame.dispose();
//                if (username.equals("admin")) {
//                    showAdminDashboard();
//                } else {
//                    showCustomerDashboard(username);
//                }
//            } else {
//                messageLabel.setText("Invalid credentials!");
//            }
//        });
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            String phone="";

            try {
                // Check credentials in the database
                String query = "SELECT role FROM users WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // If credentials are valid, get the user's role
                    String role = rs.getString("role");
                    frame.dispose();

                    if ("admin".equalsIgnoreCase(role)) {
                        showAdminDashboard();
                    } else {
                        showCustomerDashboard(username);
                    }
                } else {
                    // Invalid credentials
                    messageLabel.setText("Invalid credentials!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });


        frame.add(panel);
        frame.setVisible(true);
    }





    private static void showCreateUserScreen() {
        JFrame frame = new JFrame("Create User Account");
        frame.setSize(300, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 20, 100, 25);
        panel.add(usernameLabel);

        JTextField usernameField = new JTextField(20);
        usernameField.setBounds(120, 20, 150, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 60, 100, 25);
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setBounds(120, 60, 150, 25);
        panel.add(passwordField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(20, 100, 100, 25);
        panel.add(phoneLabel);

        JTextField phoneField = new JTextField(20);
        phoneField.setBounds(120, 100, 150, 25);
        panel.add(phoneField);

        JButton createButton = new JButton("Create");
        createButton.setBounds(20, 140, 250, 25);
        panel.add(createButton);

        JButton backButton = new JButton("Back to Login");
        backButton.setBounds(20, 180, 250, 25);
        panel.add(backButton);

        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText();

            if (!username.isEmpty() && !password.isEmpty() && !phone.isEmpty()) {
                try {
                    // Insert into database
                    String query = "INSERT INTO users (username, password, role, phone) VALUES (?, ?, 'user', ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, phone);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "User account created successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "All fields are required!");
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void showAdminDashboard() {
        JFrame frame = new JFrame("Admin Dashboard");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        // Table to show bookings
        String[] columnNames = {"ID", "Date", "Status", "Customer Name", "Phone"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Load bookings into the table
        for (Booking booking : bookings) {
            Object[] row = {booking.id, booking.date, booking.status, booking.customer_name, booking.phone_number};
            tableModel.addRow(row);
        }

        JPanel buttonPanel = new JPanel();
        JButton approveButton = new JButton("Approve");
        JButton cancelButton = new JButton("Cancel");
        JButton completeButton = new JButton("Complete");
        buttonPanel.add(approveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(completeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

//        approveButton.addActionListener(e -> {
//            int row = table.getSelectedRow();
//            if (row != -1) {
//                int bookingId = (int) table.getValueAt(row, 0);
//                try {
//                    String query = "UPDATE bookings SET status = 'Booked' WHERE id = ?";
//                    PreparedStatement stmt = conn.prepareStatement(query);
//                    stmt.setInt(1, bookingId);
//                    stmt.executeUpdate();
//                    JOptionPane.showMessageDialog(frame, "Booking approved!");
//                    loadBookingsFromDatabase();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        cancelButton.addActionListener(e -> {
//            int row = table.getSelectedRow();
//            if (row != -1) {
//                int bookingId = (int) table.getValueAt(row, 0);
//                try {
//                    String query = "UPDATE bookings SET status = 'Cancelled' WHERE id = ?";
//                    PreparedStatement stmt = conn.prepareStatement(query);
//                    stmt.setInt(1, bookingId);
//                    stmt.executeUpdate();
//                    JOptionPane.showMessageDialog(frame, "Booking cancelled!");
//                    loadBookingsFromDatabase();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        completeButton.addActionListener(e -> {
//            int row = table.getSelectedRow();
//            if (row != -1) {
//                int bookingId = (int) table.getValueAt(row, 0);
//                try {
//                    String query = "UPDATE bookings SET status = 'Completed' WHERE id = ?";
//                    PreparedStatement stmt = conn.prepareStatement(query);
//                    stmt.setInt(1, bookingId);
//                    stmt.executeUpdate();
//                    JOptionPane.showMessageDialog(frame, "Booking completed!");
//                    loadBookingsFromDatabase();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });

        approveButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int bookingId = (int) table.getValueAt(row, 0);
                try {
                    String query = "UPDATE bookings SET status = 'Booked' WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, bookingId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Booking approved!");
                    refreshAdminTable(tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        cancelButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int bookingId = (int) table.getValueAt(row, 0);
                try {
                    String query = "UPDATE bookings SET status = 'Cancelled' WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, bookingId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Booking cancelled!");
                    refreshAdminTable(tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        completeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int bookingId = (int) table.getValueAt(row, 0);
                try {
                    String query = "UPDATE bookings SET status = 'Completed' WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, bookingId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Booking completed!");
                    refreshAdminTable(tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });


        frame.add(panel);
        frame.setVisible(true);
    }

    private static void refreshAdminTable(DefaultTableModel tableModel) throws SQLException {
        loadBookingsFromDatabase(); // Reload data from the database
        tableModel.setRowCount(0);  // Clear the existing rows in the table
        for (Booking booking : bookings) {
            Object[] row = {booking.id, booking.date, booking.status, booking.customer_name, booking.phone_number};
            tableModel.addRow(row);
        }
    }


    private static void refreshCustomerTable(String username, DefaultTableModel tableModel) throws SQLException {
        loadBookingsFromDatabase(); // Reload data from the database
        tableModel.setRowCount(0);  // Clear the existing rows in the table
        for (Booking booking : bookings) {
            if (booking.customer_name.equals(username)) {
                Object[] row = {booking.date, booking.status, booking.customer_name, booking.phone_number};
                tableModel.addRow(row);
            }
        }
    }




    private static void showCustomerDashboard(String username) {
        JFrame frame = new JFrame("Customer Dashboard");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        // Table to show customer bookings
        String[] columnNames = {"Date", "Status", "Customer Name", "Phone"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Load bookings for the customer into the table
        for (Booking booking : bookings) {
            if (booking.customer_name.equals(username)) {
                Object[] row = {booking.date, booking.status, booking.customer_name, booking.phone_number};
                tableModel.addRow(row);
            }
        }

        JPanel buttonPanel = new JPanel();
        JButton bookButton = new JButton("Book New Date");
        JButton cancelButton = new JButton("Cancel Booking");
        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

//        bookButton.addActionListener(e -> {
//            String date = JOptionPane.showInputDialog(frame, "Enter Date (YYYY-MM-DD):");
//            if (date != null && !date.isEmpty()) {
//                try {
//                    // Add new booking
//                    String query = "INSERT INTO bookings (date, status, customer_name, phone_number) VALUES (?, 'Pending', ?, ?)";
//                    PreparedStatement stmt = conn.prepareStatement(query);
//                    stmt.setString(1, date);
//                    stmt.setString(2, username);
//                    stmt.setString(3, "1234567890");  // Assuming customer phone is available
//                    stmt.executeUpdate();
//                    JOptionPane.showMessageDialog(frame, "Booking request sent!");
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        cancelButton.addActionListener(e -> {
//            int row = table.getSelectedRow();
//            if (row != -1) {
//                String bookingDate = (String) table.getValueAt(row, 0);
//                String status = (String) table.getValueAt(row, 1);
//                String customerName = (String) table.getValueAt(row, 2);
//
//                if (status.equals("Pending") || status.equals("Booked")) {
//                    try {
//                        // Cancel booking if it's the customer's booking
//                        String query = "DELETE FROM bookings WHERE date = ? AND customer_name = ?";
//                        PreparedStatement stmt = conn.prepareStatement(query);
//                        stmt.setString(1, bookingDate);
//                        stmt.setString(2, customerName);
//                        stmt.executeUpdate();
//                        JOptionPane.showMessageDialog(frame, "Booking cancelled!");
//                    } catch (SQLException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
        bookButton.addActionListener(e -> {
            String date = JOptionPane.showInputDialog(frame, "Enter Date (YYYY-MM-DD):");
            if (date != null && !date.isEmpty()) {
                try {

                    //
                    //
                    //
                    //
                    // select get phone number from user table


                    String q = "SELECT phone FROM users WHERE username = ?";
                    PreparedStatement stm = conn.prepareStatement(q);
                    stm.setString(1, username);  // Set the username parameter
                    ResultSet rs = stm.executeQuery();  // Execute the query

                    String phone = null;
                    if (rs.next()) {
                        phone = rs.getString("phone");  // Get the phone number from the result set
                    }


                    String query = "INSERT INTO bookings (date, status, customer_name, phone_number) VALUES (?, 'Pending', ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);







                    stmt.setString(1, date);
                    stmt.setString(2, username);
                    stmt.setString(3, phone );  // Replace with actual customer phone
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Booking request sent!");
                    refreshCustomerTable(username, tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        cancelButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String bookingDate = (String) table.getValueAt(row, 0);
                String customerName = (String) table.getValueAt(row, 2);
                if (customerName.equals(username)) {
                    try {
                        String query = "DELETE FROM bookings WHERE date = ? AND customer_name = ?";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        stmt.setString(1, bookingDate);
                        stmt.setString(2, customerName);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Booking cancelled!");
                        refreshCustomerTable(username, tableModel);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        frame.add(panel);
        frame.setVisible(true);
    }




}



// Helper classes for User and Booking
class User {
    String username;
    String password;
    String role;
    String phone;

    public User(String username, String password, String role, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
    }
}

class Booking {
    int id;
    String date;
    String status;
    String customer_name;
    String phone_number;

    public Booking(int id, String date, String status, String customer_name, String phone_number) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.customer_name = customer_name;
        this.phone_number = phone_number;
    }
}
