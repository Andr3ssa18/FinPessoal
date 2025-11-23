package com.example.finpessoal.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

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

    public Transaction(int userId, String title, String category, double amount, String type, String date, String notes) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.notes = notes;
        this.createdAt = new java.util.Date().getTime();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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