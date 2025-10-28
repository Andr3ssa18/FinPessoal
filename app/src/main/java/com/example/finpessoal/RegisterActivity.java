package com.example.finpessoal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Configurar o comportamento do botão voltar
        setupBackPressedCallback();

        // Inicializar views
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
        // Validação do nome
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

        // Validação do email
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

        // Validação da senha
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

        // Verificar força da senha
        if (isPasswordTooSimple(password)) {
            showError("A senha é muito fraca. Use letras, números e símbolos");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isPasswordTooSimple(String password) {
        // Verificar se a senha contém apenas números
        if (password.matches("[0-9]+")) {
            return true;
        }

        // Verificar se a senha contém apenas letras
        if (password.matches("[a-zA-Z]+")) {
            return true;
        }

        // Verificar se a senha contém apenas letras minúsculas
        if (password.equals(password.toLowerCase()) && password.matches("[a-z]+")) {
            return true;
        }

        // Verificar senhas comuns
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

        // Verificar padrões sequenciais
        if (isSequential(password)) {
            return true;
        }

        return false;
    }

    private boolean isSequential(String password) {
        // Verificar sequências numéricas
        String sequentialNumbers = "012345678901234567890";
        if (sequentialNumbers.contains(password) && password.length() >= 3) {
            return true;
        }

        // Verificar sequências de teclado
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
        // Mostrar feedback visual
        showSuccess("Processando cadastro...");
        setRegisterButtonEnabled(false);

        // Simular processamento assíncrono
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        processRegistration(name, email, password);
                    }
                },
                1500);
    }

    private void processRegistration(String name, String email, String password) {
        try {
            // Verificar se o email já está em uso (em app real, verificaria no banco)
            if (isEmailAlreadyRegistered(email)) {
                showError("Este email já está cadastrado");
                setRegisterButtonEnabled(true);
                return;
            }

            // Simular criação de usuário no banco de dados
            User newUser = createUserInDatabase(name, email, password);

            if (newUser != null) {
                completeRegistrationSuccess(name, email, password);
            } else {
                showError("Erro ao criar conta. Tente novamente.");
                setRegisterButtonEnabled(true);
            }

        } catch (Exception e) {
            showError("Erro inesperado. Tente novamente.");
            setRegisterButtonEnabled(true);
        }
    }

    private boolean isEmailAlreadyRegistered(String email) {
        // Em app real, aqui verificaria no banco de dados ou Firebase
        // Por enquanto, retornamos false para permitir o cadastro
        return false;
    }

    private User createUserInDatabase(String name, String email, String password) {
        // Simular criação de usuário no banco
        // Em app real, aqui salvaria no Firebase ou banco local
        return new User(name, email, password);
    }

    private void completeRegistrationSuccess(String name, String email, String password) {
        // Preparar resultado para MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("REGISTER_NAME", name);
        resultIntent.putExtra("REGISTER_EMAIL", email);
        resultIntent.putExtra("REGISTER_PASSWORD", password);
        setResult(RESULT_OK, resultIntent);

        // Mostrar sucesso
        showSuccess("Cadastro realizado com sucesso! ✅");

        // Voltar para MainActivity após breve delay
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        finishWithTransition();
                    }
                },
                1000);
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

        // Personalizar snackbar de erro
        snackbar.setBackgroundTint(getColor(android.R.color.holo_red_dark))
                .setTextColor(getColor(android.R.color.white))
                .setActionTextColor(getColor(android.R.color.white));

        // Adicionar ação para limpar campos em caso de erro
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
                .setBackgroundTint(getColor(android.R.color.holo_green_dark))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void setRegisterButtonEnabled(boolean enabled) {
        btnRegister.setEnabled(enabled);
        if (enabled) {
            btnRegister.setText("Cadastrar");
            btnRegister.setAlpha(1.0f);
            btnRegister.setBackgroundColor(getColor(R.color.primaryYellow));
        } else {
            btnRegister.setText("Cadastrando...");
            btnRegister.setAlpha(0.7f);
        }
    }

    // Método para limpar todos os campos
    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etName.requestFocus();

        showSuccess("Campos limpos. Preencha novamente.");
    }

    // Método para validar email em tempo real (opcional)
    private boolean validateEmailInRealTime(String email) {
        if (email.isEmpty()) return true; // Permitir campo vazio durante digitação

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Método para mostrar força da senha (opcional)
    private String getPasswordStrength(String password) {
        if (password.length() < 6) return "Muito fraca";
        if (password.length() < 8) return "Fraca";
        if (password.matches(".*[0-9].*") && password.matches(".*[a-zA-Z].*")) {
            if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                return "Muito forte";
            }
            return "Forte";
        }
        return "Média";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar handlers se necessário
    }

    // Métodos auxiliares para testes
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