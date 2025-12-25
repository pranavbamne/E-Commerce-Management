package com.ecommerce.gui;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.User;
import com.ecommerce.model.Product;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@SuppressWarnings("serial")
public class CustomerDashboard extends JFrame {
    private User currentUser;
    private ProductDAO productDAO;
    private CartDAO cartDAO;
    private OrderDAO orderDAO;
    private JTabbedPane tabbedPane;
    
    public CustomerDashboard(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Customer Dashboard - " + currentUser.getFullName());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", createProductPanel());
        tabbedPane.addTab("My Cart", createCartPanel());
        tabbedPane.addTab("My Orders", createOrderPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
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
        
        String[] columns = {"ID", "Product Name", "Description", "Price", "Stock"};
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
        
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.setBackground(new Color(60, 179, 113));
        addToCartButton.setForeground(Color.BLACK);
        addToCartButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) model.getValueAt(selectedRow, 0);
                int stock = Integer.parseInt(model.getValueAt(selectedRow, 4).toString());
                
                if (stock <= 0) {
                    JOptionPane.showMessageDialog(this, "Product out of stock");
                    return;
                }
                
                String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
                if (quantityStr != null) {
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity > 0 && quantity <= stock) {
                            if (cartDAO.addToCart(currentUser.getUserId(), productId, quantity)) {
                                JOptionPane.showMessageDialog(this, "Added to cart successfully");
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid quantity");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid number");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product");
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProducts(model));
        
        buttonPanel.add(addToCartButton);
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
                product.getStockQuantity()
            });
        }
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"Cart ID", "Product", "Price", "Quantity", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        JLabel totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        loadCart(model, totalLabel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton removeButton = new JButton("Remove Item");
        removeButton.setBackground(new Color(220, 20, 60));
        removeButton.setForeground(Color.BLACK);
        removeButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int cartId = (int) model.getValueAt(selectedRow, 0);
                if (cartDAO.removeFromCart(cartId)) {
                    JOptionPane.showMessageDialog(this, "Item removed from cart");
                    loadCart(model, totalLabel);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item");
            }
        });
        
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(60, 179, 113));
        checkoutButton.setForeground(Color.BLACK);
        checkoutButton.addActionListener(e -> checkout(model, totalLabel));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCart(model, totalLabel));
        
        buttonPanel.add(removeButton);
        buttonPanel.add(checkoutButton);
        buttonPanel.add(refreshButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadCart(DefaultTableModel model, JLabel totalLabel) {
        model.setRowCount(0);
        List<CartItem> cartItems = cartDAO.getCartItems(currentUser.getUserId());
        double total = 0;
        
        for (CartItem item : cartItems) {
            model.addRow(new Object[]{
                item.getCartId(),
                item.getProductName(),
                String.format("₹%.2f", item.getPrice()),
                item.getQuantity(),
                String.format("₹%.2f", item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        
        totalLabel.setText(String.format("Total: ₹%.2f", total));
    }
    
    private void checkout(DefaultTableModel model, JLabel totalLabel) {
        List<CartItem> cartItems = cartDAO.getCartItems(currentUser.getUserId());
        
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty");
            return;
        }
        
        String address = JOptionPane.showInputDialog(this, "Enter shipping address:");
        if (address != null && !address.trim().isEmpty()) {
            double total = 0;
            for (CartItem item : cartItems) {
                total += item.getSubtotal();
            }
            
            Order order = new Order();
            order.setUserId(currentUser.getUserId());
            order.setTotalAmount(total);
            order.setStatus("PENDING");
            order.setShippingAddress(address);
            
            if (orderDAO.createOrder(order, cartItems)) {
                cartDAO.clearCart(currentUser.getUserId());
                JOptionPane.showMessageDialog(this, "Order placed successfully!");
                loadCart(model, totalLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to place order");
            }
        }
    }
    
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"Order ID", "Date", "Amount", "Status", "Address"};
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
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadOrders(model));
        
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        List<Order> orders = orderDAO.getOrdersByUser(currentUser.getUserId());
        for (Order order : orders) {
            model.addRow(new Object[]{
                order.getOrderId(),
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
