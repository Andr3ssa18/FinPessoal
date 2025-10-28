package com.example.finpessoal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private EditText etName, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSaveProfile, btnChangePassword, btnBack;

    private String currentUserName;
    private String currentUserEmail;
    private String currentPassword = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Configurar o comportamento do botão voltar
        setupBackPressedCallback();

        // Inicializar views
        initializeViews();

        // Obter dados do usuário
        getUserData();

        // Configurar perfil
        setupProfile();
        setupClickListeners();
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToDashboard();
            }
        });
    }

    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void getUserData() {
        Intent intent = getIntent();
        currentUserName = intent.getStringExtra("USER_NAME");
        currentUserEmail = intent.getStringExtra("USER_EMAIL");

        if (currentUserName == null || currentUserName.isEmpty()) {
            currentUserName = "Usuário";
        }
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            currentUserEmail = "usuario@email.com";
        }
    }

    private void setupProfile() {
        tvUserName.setText(currentUserName);
        tvUserEmail.setText(currentUserEmail);
        etName.setText(currentUserName);
        etEmail.setText(currentUserEmail);
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToDashboard();
            }
        });
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (!validateProfileInputs(newName, newEmail)) {
            return;
        }

        if (newName.equals(currentUserName) && newEmail.equals(currentUserEmail)) {
            showInfo("Nenhuma alteração foi feita.");
            return;
        }

        showConfirmationDialog("Salvar Alterações",
                "Deseja salvar as alterações no perfil?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateProfile(newName, newEmail);
                    }
                });
    }

    private boolean validateProfileInputs(String name, String email) {
        if (TextUtils.isEmpty(name)) {
            showError("Por favor, digite seu nome");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            showError("O nome deve ter pelo menos 2 caracteres");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            showError("Por favor, digite seu email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor, digite um email válido");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void updateProfile(String newName, String newEmail) {
        currentUserName = newName;
        currentUserEmail = newEmail;

        tvUserName.setText(newName);
        tvUserEmail.setText(newEmail);

        showSuccess("Perfil atualizado com sucesso!");
    }

    private void changePassword() {
        String currentPasswordInput = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validatePasswordInputs(currentPasswordInput, newPassword, confirmPassword)) {
            return;
        }

        if (!currentPasswordInput.equals(currentPassword)) {
            showError("Senha atual incorreta");
            etCurrentPassword.requestFocus();
            return;
        }

        if (currentPasswordInput.equals(newPassword)) {
            showError("A nova senha deve ser diferente da senha atual");
            etNewPassword.requestFocus();
            return;
        }

        showConfirmationDialog("Alterar Senha",
                "Deseja alterar sua senha?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePassword(newPassword);
                    }
                });
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            showError("Por favor, digite sua senha atual");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showError("Por favor, digite a nova senha");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            showError("A nova senha deve ter pelo menos 6 caracteres");
            etNewPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("As senhas não coincidem");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void updatePassword(String newPassword) {
        currentPassword = newPassword;

        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");

        showSuccess("Senha alterada com sucesso!");
    }

    private void returnToDashboard() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_NAME", currentUserName);
        resultIntent.putExtra("UPDATED_EMAIL", currentUserEmail);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showConfirmationDialog(String title, String message,
                                        DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sim", positiveListener)
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void showError(String message) {
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, "✅ " + message, Toast.LENGTH_SHORT).show();
    }

    private void showInfo(String message) {
        Toast.makeText(this, "ℹ️ " + message, Toast.LENGTH_SHORT).show();
    }
}