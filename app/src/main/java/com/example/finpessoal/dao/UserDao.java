package com.example.finpessoal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;



import com.example.finpessoal.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);
}
