package com.example.finpessoal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.UserDao;
import com.example.finpessoal.entities.User;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private AppDatabase db;
    private UserDao userDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;

    private static final int REGISTER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        setupClickListeners();
        addSampleUsersToDatabase();
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (validateInputs(email, password)) {
                    attemptLogin(email, password);
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegister();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showError("Por favor, digite seu email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError("Por favor, digite sua senha");
            etPassword.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor, digite um email válido");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void attemptLogin(String email, String password) {
        setLoginButtonEnabled(false);

        executorService.execute(() -> {
            try {
                User foundUser = userDao.login(email, password);

                runOnUiThread(() -> {
                    setLoginButtonEnabled(true);

                    if (foundUser != null) {
                        sessionManager.createSession(foundUser.getId(), foundUser.getName(), foundUser.getEmail());
                        showSuccess("Login realizado com sucesso!");
                        navigateToDashboard();
                    } else {
                        checkIfEmailExists(email);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoginButtonEnabled(true);
                    showError("Erro ao fazer login: " + e.getMessage());
                });
            }
        });
    }

    private void checkIfEmailExists(String email) {
        executorService.execute(() -> {
            try {
                User userByEmail = userDao.getUserByEmail(email);

                runOnUiThread(() -> {
                    if (userByEmail != null) {
                        showError("Senha incorreta");
                    } else {
                        showError("Usuário não encontrado. Cadastre-se primeiro.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Erro ao verificar usuário: " + e.getMessage());
                });
            }
        });
    }

    private void setLoginButtonEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        if (enabled) {
            btnLogin.setText("Entrar");
            btnLogin.setAlpha(1.0f);
        } else {
            btnLogin.setText("Entrando...");
            btnLogin.setAlpha(0.7f);
        }
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER_REQUEST_CODE);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showError(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                .setTextColor(getResources().getColor(android.R.color.white))
                .show();
    }

    private void showSuccess(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                .setTextColor(getResources().getColor(android.R.color.white))
                .show();
    }

    private void addSampleUsersToDatabase() {
        executorService.execute(() -> {
            try {
                long userCount = userDao.getAllUsers().size();

                if (userCount == 0) {
                    String currentTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());

                    User user1 = new User("João Silva", "joao@email.com", "123456", currentTime);
                    User user2 = new User("Maria Santos", "maria@email.com", "abcdef", currentTime);
                    User user3 = new User("Admin", "admin@email.com", "admin123", currentTime);

                    userDao.insert(user1);
                    userDao.insert(user2);
                    userDao.insert(user3);

                    System.out.println("Usuários de exemplo adicionados ao banco de dados");
                }
            } catch (Exception e) {
                System.out.println("Erro ao adicionar usuários de exemplo: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String email = data.getStringExtra("REGISTER_EMAIL");
                if (email != null) {
                    showSuccess("Cadastro realizado! Faça login com suas credenciais.");
                    etEmail.setText(email);
                    etPassword.setText("");
                    etPassword.requestFocus();
                }
            }
        } else if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            showError("Cadastro cancelado.");
        }
    }

    public void clearFields() {
        etEmail.setText("");
        etPassword.setText("");
        etEmail.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}