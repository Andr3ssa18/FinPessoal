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

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private List<User> users = new ArrayList<>();

    // Código de requisição para registro
    private static final int REGISTER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        setupClickListeners();
        addSampleUsers();
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
            return false;
        }

        if (password.isEmpty()) {
            showError("Por favor, digite sua senha");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor, digite um email válido");
            return false;
        }

        return true;
    }

    private void attemptLogin(String email, String password) {
        User foundUser = null;
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                foundUser = user;
                break;
            }
        }

        if (foundUser != null) {
            showSuccess("Login realizado com sucesso!");
            navigateToDashboard(foundUser);
        } else {
            boolean emailExists = false;
            for (User user : users) {
                if (user.getEmail().equals(email)) {
                    emailExists = true;
                    break;
                }
            }

            if (emailExists) {
                showError("Senha incorreta");
            } else {
                showError("Usuário não encontrado. Cadastre-se primeiro.");
            }
        }
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER_REQUEST_CODE);
    }

    private void navigateToDashboard(User user) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("USER_EMAIL", user.getEmail());
        intent.putExtra("USER_NAME", user.getName());
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(android.R.color.holo_red_dark))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void showSuccess(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(android.R.color.holo_green_dark))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void addSampleUsers() {
        users.add(new User("João Silva", "joao@email.com", "123456"));
        users.add(new User("Maria Santos", "maria@email.com", "abcdef"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra("REGISTER_NAME");
                String email = data.getStringExtra("REGISTER_EMAIL");
                String password = data.getStringExtra("REGISTER_PASSWORD");

                if (name != null && email != null && password != null) {
                    // Verificar se o usuário já existe
                    boolean userExists = false;
                    for (User user : users) {
                        if (user.getEmail().equals(email)) {
                            userExists = true;
                            break;
                        }
                    }

                    if (!userExists) {
                        users.add(new User(name, email, password));
                        showSuccess("Cadastro realizado! Faça login com suas credenciais.");

                        // Preencher automaticamente os campos
                        etEmail.setText(email);
                        etPassword.setText("");
                        etPassword.requestFocus();
                    } else {
                        showError("Este email já está cadastrado.");
                    }
                }
            }
        }
    }

    // Método para limpar campos (opcional)
    public void clearFields() {
        etEmail.setText("");
        etPassword.setText("");
        etEmail.requestFocus();
    }
}