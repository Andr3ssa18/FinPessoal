package com.example.finpessoal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public Transaction(String title, String category, double amount, String type, String date, String notes) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.notes = notes;
        this.createdAt = new Date().getTime();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}