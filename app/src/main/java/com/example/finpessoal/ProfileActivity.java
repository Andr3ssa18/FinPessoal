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

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.UserDao;
import com.example.finpessoal.entities.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private EditText etName, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSaveProfile, btnChangePassword, btnLogout;
    private BottomNavigationView bottomNavigation;

    private AppDatabase db;
    private UserDao userDao;
    private ExecutorService executorService;
    private SessionManager sessionManager;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();

        setupBackPressedCallback();
        initializeViews();
        loadUserData();
        setupBottomNavigation();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                navigateToDashboard();
                return true;
            } else if (itemId == R.id.menu_transactions) {
                navigateToAddTransaction();
                return true;
            } else if (itemId == R.id.menu_reports) {
                navigateToRelatorios();
                return true;
            } else if (itemId == R.id.menu_profile) {
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.menu_profile);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToAddTransaction() {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRelatorios() {
        Intent intent = new Intent(this, RelatoriosActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        executorService.execute(() -> {
            try {
                int userId = sessionManager.getUserId();
                currentUser = userDao.getUserByEmail(sessionManager.getUserEmail());

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        setupProfile();
                        setupClickListeners();
                    } else {
                        showError("Usuário não encontrado");
                        finish();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Erro ao carregar dados: " + e.getMessage());
                });
            }
        });
    }

    private void setupProfile() {
        if (currentUser != null) {
            tvUserName.setText(currentUser.getName());
            tvUserEmail.setText(currentUser.getEmail());
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail());
        }
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

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (!validateProfileInputs(newName, newEmail)) {
            return;
        }

        if (newName.equals(currentUser.getName()) && newEmail.equals(currentUser.getEmail())) {
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
        executorService.execute(() -> {
            try {
                if (!newEmail.equals(currentUser.getEmail())) {
                    User existingUser = userDao.getUserByEmail(newEmail);
                    if (existingUser != null) {
                        runOnUiThread(() -> {
                            showError("Este email já está em uso por outro usuário");
                        });
                        return;
                    }
                }

                currentUser.setName(newName);
                currentUser.setEmail(newEmail);
                userDao.update(currentUser);

                runOnUiThread(() -> {
                    tvUserName.setText(newName);
                    tvUserEmail.setText(newEmail);
                    showSuccess("Perfil atualizado com sucesso!");

                    sessionManager.createSession(currentUser.getId(), newName, newEmail);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Erro ao atualizar perfil: " + e.getMessage());
                });
            }
        });
    }

    private void changePassword() {
        String currentPasswordInput = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validatePasswordInputs(currentPasswordInput, newPassword, confirmPassword)) {
            return;
        }

        if (!currentPasswordInput.equals(currentUser.getPassword())) {
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
        executorService.execute(() -> {
            try {
                currentUser.setPassword(newPassword);
                userDao.update(currentUser);

                runOnUiThread(() -> {
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                    showSuccess("Senha alterada com sucesso!");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError("Erro ao alterar senha: " + e.getMessage());
                });
            }
        });
    }

    private void returnToDashboard() {
        finish();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sair")
                .setMessage("Tem certeza que deseja sair?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionManager.logout();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showInfo(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.menu_profile);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}