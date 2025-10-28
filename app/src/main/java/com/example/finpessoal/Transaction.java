package com.example.finpessoal;

import java.util.Date;

public class Transaction {
    private String title;
    private String category;
    private double amount;
    private Date date;

    public Transaction(String title, String category, double amount, Date date) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // Getters
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public Date getDate() { return date; }

    // Setters (opcionais)
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(Date date) { this.date = date; }
}