package com.ecommerce.gui;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.Product;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class AddProductDialog extends JDialog {
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JTextField stockField;
    private JTextField categoryField;
    private ProductDAO productDAO;
    
    public AddProductDialog(JFrame parent, ProductDAO productDAO) {
        super(parent, "Add Product", true);
        this.productDAO = productDAO;
        initComponents();
    }
    
    private void initComponents() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Product Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Price:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        priceField = new JTextField(20);
        formPanel.add(priceField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Stock Quantity:"), gbc);
        
        gbc.gridx = 1;
        stockField = new JTextField(20);
        formPanel.add(stockField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Category ID:"), gbc);
        
        gbc.gridx = 1;
        categoryField = new JTextField(20);
        formPanel.add(categoryField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(e -> saveProduct());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(169, 169, 169));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void saveProduct() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            int categoryId = Integer.parseInt(categoryField.getText().trim());
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product name is required");
                return;
            }
            
            Product product = new Product();
            product.setProductName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stock);
            product.setCategoryId(categoryId);
            
            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product added successfully");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price, stock, and category ID");
        }
    }
}