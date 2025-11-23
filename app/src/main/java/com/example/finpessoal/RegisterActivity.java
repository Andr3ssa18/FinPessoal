package com.example.finpessoal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.UserDao;
import com.example.finpessoal.entities.User;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private AppDatabase db;
    private UserDao userDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sessionManager = new SessionManager(this);
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
        setupBackPressedCallback();
        initializeViews();
        setupClickListeners();
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        });
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateRegistration(name, email, password, confirmPassword)) {
            registerUser(name, email, password);
        }
    }

    private boolean validateRegistration(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            showError("Por favor, digite seu nome completo");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            showError("O nome deve ter pelo menos 3 caracteres");
            etName.requestFocus();
            return false;
        }

        if (name.length() > 50) {
            showError("O nome é muito longo (máximo 50 caracteres)");
            etName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            showError("Por favor, digite seu email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor, digite um email válido");
            etEmail.requestFocus();
            return false;
        }

        if (email.length() > 100) {
            showError("O email é muito longo");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError("Por favor, digite uma senha");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showError("A senha deve ter pelo menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() > 20) {
            showError("A senha deve ter no máximo 20 caracteres");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("As senhas não coincidem");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (isPasswordTooSimple(password)) {
            showError("A senha é muito fraca. Use letras, números e símbolos");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isPasswordTooSimple(String password) {
        if (password.matches("[0-9]+")) {
            return true;
        }

        if (password.matches("[a-zA-Z]+")) {
            return true;
        }

        if (password.equals(password.toLowerCase()) && password.matches("[a-z]+")) {
            return true;
        }

        String[] commonPasswords = {
                "123456", "12345678", "123456789", "1234567", "1234567890",
                "abcdef", "password", "senha123", "qwerty", "abc123",
                "111111", "123123", "admin", "letmein", "welcome"
        };

        for (String common : commonPasswords) {
            if (password.equalsIgnoreCase(common)) {
                return true;
            }
        }

        if (isSequential(password)) {
            return true;
        }

        return false;
    }

    private boolean isSequential(String password) {
        String sequentialNumbers = "012345678901234567890";
        if (sequentialNumbers.contains(password) && password.length() >= 3) {
            return true;
        }

        String[] keyboardSequences = {
                "qwerty", "asdfgh", "zxcvbn", "qazwsx", "edcrfv"
        };

        for (String sequence : keyboardSequences) {
            if (sequence.contains(password.toLowerCase()) && password.length() >= 3) {
                return true;
            }
        }

        return false;
    }

    private void registerUser(String name, String email, String password) {
        showSuccess("Processando cadastro...");
        setRegisterButtonEnabled(false);

        executorService.execute(() -> {
            processRegistration(name, email, password);
        });
    }

    private void processRegistration(String name, String email, String password) {
        try {
            User existingUser = userDao.getUserByEmail(email);
            if (existingUser != null) {
                runOnUiThread(() -> {
                    showError("Este email já está cadastrado");
                    setRegisterButtonEnabled(true);
                });
                return;
            }

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            User newUser = new User(name, email, password, currentTime);
            long userId = userDao.insert(newUser);

            if (userId > 0) {
                runOnUiThread(() -> completeRegistrationSuccess(newUser));
            } else {
                runOnUiThread(() -> {
                    showError("Erro ao criar conta. Tente novamente.");
                    setRegisterButtonEnabled(true);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                showError("Erro inesperado: " + e.getMessage());
                setRegisterButtonEnabled(true);
            });
        }
    }

    private void completeRegistrationSuccess(User user) {
        sessionManager.createSession(user.getId(), user.getName(), user.getEmail());
        showSuccess("Cadastro realizado com sucesso!");
        new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        navigateToDashboard();
                    }
                },
                1000);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToLogin() {
        setResult(RESULT_CANCELED);
        finishWithTransition();
    }

    private void finishWithTransition() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showError(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);

        snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                .setTextColor(getResources().getColor(android.R.color.white))
                .setActionTextColor(getResources().getColor(android.R.color.white));

        if (message.contains("email já está cadastrado")) {
            snackbar.setAction("Limpar", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearForm();
                }
            });
        }

        snackbar.show();
    }

    private void showSuccess(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                .setTextColor(getResources().getColor(android.R.color.white))
                .show();
    }

    private void setRegisterButtonEnabled(boolean enabled) {
        runOnUiThread(() -> {
            btnRegister.setEnabled(enabled);
            if (enabled) {
                btnRegister.setText("Cadastrar");
                btnRegister.setAlpha(1.0f);
            } else {
                btnRegister.setText("Cadastrando...");
                btnRegister.setAlpha(0.7f);
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etName.requestFocus();

        showSuccess("Campos limpos. Preencha novamente.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public void setTestData() {
        etName.setText("João Silva Teste");
        etEmail.setText("joao@teste.com");
        etPassword.setText("Senha123!");
        etConfirmPassword.setText("Senha123!");
    }

    public void simulateNetworkError() {
        showError("Erro de conexão. Verifique sua internet.");
        setRegisterButtonEnabled(true);
    }

    public void simulateServerError() {
        showError("Servidor indisponível. Tente novamente mais tarde.");
        setRegisterButtonEnabled(true);
    }
}