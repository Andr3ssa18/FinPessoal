package com.example.finpessoal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.finpessoal.entities.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY created_at DESC")
    List<Transaction> getAllTransactions(int userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    List<Transaction> getTransactionsByDateRange(int userId, String startDate, String endDate);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = :type ORDER BY created_at DESC")
    List<Transaction> getTransactionsByType(int userId, String type);

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    List<Transaction> getRecentTransactions(int userId, int limit);

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Receita'")
    double getTotalIncome(int userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Despesa'")
    double getTotalExpenses(int userId);

    @Query("DELETE FROM transactions WHERE id = :id AND user_id = :userId")
    void deleteById(int id, int userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = 'Receita' ORDER BY amount DESC LIMIT 1")
    Transaction getLargestIncome(int userId);

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND type = 'Despesa' ORDER BY amount DESC LIMIT 1")
    Transaction getLargestExpense(int userId);

    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId")
    int getTransactionsCount(int userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Receita' AND strftime('%m', date) = strftime('%m', 'now') AND strftime('%Y', date) = strftime('%Y', 'now')")
    double getCurrentMonthIncome(int userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE user_id = :userId AND type = 'Despesa' AND strftime('%m', date) = strftime('%m', 'now') AND strftime('%Y', date) = strftime('%Y', 'now')")
    double getCurrentMonthExpenses(int userId);
}