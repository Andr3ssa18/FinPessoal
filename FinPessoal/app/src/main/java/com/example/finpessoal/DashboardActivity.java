package com.example.finpessoal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvDate, tvBalance, tvBalanceChange, tvIncome, tvExpense, tvViewAll;
    private RecyclerView rvTransactions;
    private List<Transaction> transactions = new ArrayList<>();
    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddTransaction;

    private String currentUserName;
    private String currentUserEmail;

    // Códigos de requisição
    private static final int PROFILE_REQUEST_CODE = 1;
    private static final int ADD_TRANSACTION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicializar views
        initializeViews();

        // Obter dados do usuário
        currentUserName = getIntent().getStringExtra("USER_NAME");
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");

        setupDashboard(currentUserName);
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDate = findViewById(R.id.tvDate);
        tvBalance = findViewById(R.id.tvBalance);
        tvBalanceChange = findViewById(R.id.tvBalanceChange);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvViewAll = findViewById(R.id.tvViewAll);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Tentar encontrar o FAB se existir no layout
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
    }

    private void setupDashboard(String userName) {
        // Configurar boas-vindas
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Olá, " + userName + "!");
        } else {
            tvWelcome.setText("Olá, Usuário!");
        }

        // Configurar data atual
        String currentDate = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("pt", "BR")).format(new Date());
        tvDate.setText(currentDate);

        // Configurar valores financeiros
        setupFinancialData();

        // Adicionar transações iniciais (apenas se estiver vazio)
        if (transactions.isEmpty()) {
            addInitialTransactions();
        }
    }

    private void setupFinancialData() {
        // Calcular totais baseados nas transações
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        double balance = totalIncome - totalExpense;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Atualizar UI
        tvIncome.setText(format.format(totalIncome));
        tvExpense.setText(format.format(totalExpense));
        tvBalance.setText(format.format(balance));

        // Cor do saldo - usando cores do seu tema
        // DENTRO DE setupFinancialData()

        // ... (depois de NumberFormat format = ...)

        // Atualizar UI
        tvIncome.setText(format.format(totalIncome));
        tvExpense.setText(format.format(totalExpense));
        tvBalance.setText(format.format(balance));

        // Bloco de código CORRIGIDO
        if (balance >= 0) {
            // Usando o método moderno e as cores do seu projeto
            tvBalance.setTextColor(getColor(R.color.success_green));
            tvBalanceChange.setText("+12% este mês");
            tvBalanceChange.setTextColor(getColor(R.color.success_green));
        } else {
            // Usando o método moderno e as cores do seu projeto
            tvBalance.setTextColor(getColor(R.color.error_red));
            tvBalanceChange.setText("-8% este mês");
            tvBalanceChange.setTextColor(getColor(R.color.error_red));
        }

    }

    private void setupRecyclerView() {
        // Verificar se há transações antes de configurar o adapter
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        TransactionAdapter adapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Botão Sair
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Botão "Ver todas" as transações
        tvViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoreTransactions();
            }
        });

        // Configurar FAB para adicionar transação (se existir)
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddTransactionActivity();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_dashboard) {
                    showMessage("Você já está na tela inicial");
                    return true;
                } else if (itemId == R.id.menu_transactions) {
                    showMessage("Tela de Transações em desenvolvimento");
                    return true;
                } else if (itemId == R.id.menu_reports) {
                    navigateToRelatorios();
                    return true;
                } else if (itemId == R.id.menu_profile) {
                    navigateToProfile();
                    return true;
                } else if (itemId == R.id.menu_add) {
                    // Se o menu_add for clicado no BottomNavigation
                    openAddTransactionActivity();
                    return true;
                }
                return false;
            }
        });
    }

    private void openAddTransactionActivity() {
        Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
        startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE);
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sair")
                .setMessage("Tem certeza que deseja sair?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Voltar para MainActivity
                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
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

    private void addInitialTransactions() {
        // Adicionar apenas algumas transações iniciais como exemplo
        if (transactions.isEmpty()) {
            transactions.add(new Transaction("Salário", "Rendimento", 2500.00, new Date()));
            transactions.add(new Transaction("Mercado", "Alimentação", -150.75, new Date()));

            // Atualizar dados financeiros
            setupFinancialData();

            // Notificar o adapter
            if (rvTransactions.getAdapter() != null) {
                rvTransactions.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void addMoreTransactions() {
        // Adicionar mais transações quando clicar em "Ver todas"
        transactions.add(new Transaction("Uber", "Transporte", -25.50, new Date()));
        transactions.add(new Transaction("Cinema", "Lazer", -45.00, new Date()));
        transactions.add(new Transaction("Bônus", "Rendimento", 300.00, new Date()));

        // Atualizar dados financeiros
        setupFinancialData();

        // Notificar o adapter
        if (rvTransactions.getAdapter() != null) {
            rvTransactions.getAdapter().notifyDataSetChanged();
        }

        showMessage("Mais transações carregadas!");
    }

    private void navigateToRelatorios() {
        Intent intent = new Intent(this, RelatoriosActivity.class);

        // Calcular dados para relatórios
        double totalIncome = 0;
        double totalExpense = 0;
        double largestIncome = 0;
        double largestExpense = 0;
        String largestIncomeTitle = "";
        String largestExpenseTitle = "";

        for (Transaction transaction : transactions) {
            double amount = transaction.getAmount();
            if (amount > 0) {
                totalIncome += amount;
                if (amount > largestIncome) {
                    largestIncome = amount;
                    largestIncomeTitle = transaction.getTitle();
                }
            } else {
                double expense = Math.abs(amount);
                totalExpense += expense;
                if (expense > largestExpense) {
                    largestExpense = expense;
                    largestExpenseTitle = transaction.getTitle();
                }
            }
        }

        // Passar dados para RelatoriosActivity
        intent.putExtra("TOTAL_INCOME", totalIncome);
        intent.putExtra("TOTAL_EXPENSE", totalExpense);
        intent.putExtra("LARGEST_INCOME", largestIncome);
        intent.putExtra("LARGEST_EXPENSE", largestExpense);
        intent.putExtra("LARGEST_INCOME_TITLE", largestIncomeTitle);
        intent.putExtra("LARGEST_EXPENSE_TITLE", largestExpenseTitle);
        intent.putExtra("TRANSACTIONS_COUNT", transactions.size());

        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("USER_NAME", currentUserName);
        intent.putExtra("USER_EMAIL", currentUserEmail);
        startActivityForResult(intent, PROFILE_REQUEST_CODE);
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String updatedName = data.getStringExtra("UPDATED_NAME");
                String updatedEmail = data.getStringExtra("UPDATED_EMAIL");

                if (updatedName != null && !updatedName.isEmpty()) {
                    currentUserName = updatedName;
                    tvWelcome.setText("Olá, " + updatedName + "!");
                }
                if (updatedEmail != null && !updatedEmail.isEmpty()) {
                    currentUserEmail = updatedEmail;
                }

                showMessage("Perfil atualizado com sucesso!");
            }
        } else if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String title = data.getStringExtra("TRANSACTION_TITLE");
                String category = data.getStringExtra("TRANSACTION_CATEGORY");
                double amount = data.getDoubleExtra("TRANSACTION_AMOUNT", 0);

                // Adicionar nova transação no início da lista
                transactions.add(0, new Transaction(title, category, amount, new Date()));

                // Atualizar dados financeiros
                setupFinancialData();

                // Notificar o adapter
                if (rvTransactions.getAdapter() != null) {
                    rvTransactions.getAdapter().notifyDataSetChanged();
                }

                showMessage("Transação adicionada com sucesso!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualizar dados quando voltar para esta tela
        setupFinancialData();
    }
}