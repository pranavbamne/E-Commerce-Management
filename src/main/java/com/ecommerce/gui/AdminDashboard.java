package com.ecommerce.gui;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.User;
import com.ecommerce.model.Product;
import com.ecommerce.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@SuppressWarnings("serial")
public class AdminDashboard extends JFrame {
    private User currentUser;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private JTabbedPane tabbedPane;
    
    public AdminDashboard(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Admin Dashboard - " + currentUser.getFullName());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Product Management", createProductPanel());
        tabbedPane.addTab("Order Management", createOrderPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Admin)");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Name", "Description", "Price", "Stock", "Category ID"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        loadProducts(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add Product");
        addButton.setBackground(new Color(60, 179, 113));
        addButton.setForeground(Color.BLACK);
        addButton.addActionListener(e -> {
            new AddProductDialog(this, productDAO).setVisible(true);
            loadProducts(model);
        });
        
        JButton editButton = new JButton("Edit Product");
        editButton.setBackground(new Color(70, 130, 180));
        editButton.setForeground(Color.BLACK);
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) model.getValueAt(selectedRow, 0);
                Product product = productDAO.getProductById(productId);
                new EditProductDialog(this, productDAO, product).setVisible(true);
                loadProducts(model);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to edit");
            }
        });
        
        JButton deleteButton = new JButton("Delete Product");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) model.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?");
                if (confirm == JOptionPane.YES_OPTION) {
                    if (productDAO.deleteProduct(productId)) {
                        JOptionPane.showMessageDialog(this, "Product deleted successfully");
                        loadProducts(model);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to delete");
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProducts(model));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadProducts(DefaultTableModel model) {
        model.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            model.addRow(new Object[]{
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                String.format("₹%.2f", product.getPrice()),
                product.getStockQuantity(),
                product.getCategoryId()
            });
        }
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"Order ID", "User ID", "Date", "Amount", "Status", "Address"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        loadOrders(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.setBackground(new Color(70, 130, 180));
        updateStatusButton.setForeground(Color.BLACK);
        updateStatusButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int orderId = (int) model.getValueAt(selectedRow, 0);
                String[] statuses = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"};
                String status = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Order Status",
                        JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
                
                if (status != null) {
                    if (orderDAO.updateOrderStatus(orderId, status)) {
                        JOptionPane.showMessageDialog(this, "Order status updated successfully");
                        loadOrders(model);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order");
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadOrders(model));
        
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        List<Order> orders = orderDAO.getAllOrders();
        for (Order order : orders) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getUserId(),
                order.getOrderDate(),
                String.format("₹%.2f", order.getTotalAmount()),
                order.getStatus(),
                order.getShippingAddress()
            });
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?");
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}