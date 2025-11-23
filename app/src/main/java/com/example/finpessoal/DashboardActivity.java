package com.example.finpessoal;

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.TransactionDao;
import com.example.finpessoal.entities.Transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvDate, tvBalance, tvBalanceChange, tvIncome, tvExpense, tvViewAll;
    private ImageView ivTrending;
    private RecyclerView rvTransactions;
    private List<Transaction> transactions = new ArrayList<>();
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionAdapter adapter;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private int currentUserId;
    private String currentUserName;
    private String currentUserEmail;
    private AppDatabase db;
    private TransactionDao transactionDao;

    private boolean showingAllTransactions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        currentUserId = sessionManager.getUserId();
        currentUserName = sessionManager.getUserName();
        currentUserEmail = sessionManager.getUserEmail();

        db = AppDatabase.getInstance(this);
        transactionDao = db.transactionDao();

        initializeViews();
        setupDashboard(currentUserName);
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();

        loadRecentTransactions();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
        ivTrending = findViewById(R.id.ivTrending);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupDashboard(String userName) {
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Olá, " + userName + "!");
        } else {
            tvWelcome.setText("Olá, Usuário!");
        }

        String currentDate = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("pt", "BR")).format(new Date());
        tvDate.setText(currentDate);
    }

    private void loadRecentTransactions() {
        try {
            List<Transaction> recentTransactions = transactionDao.getRecentTransactions(currentUserId, 5);
            allTransactions = transactionDao.getAllTransactions(currentUserId);

            transactions.clear();
            if (recentTransactions != null && !recentTransactions.isEmpty()) {
                transactions.addAll(recentTransactions);
                System.out.println(recentTransactions.size() + " transações recentes do usuário " + currentUserId);
            } else {
                System.out.println(" Nenhuma transação encontrada para o usuário " + currentUserId);
            }

            setupFinancialData();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            updateEmptyState();
            updateViewAllButton();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar transações: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllTransactions() {
        try {
            List<Transaction> allDbTransactions = transactionDao.getAllTransactions(currentUserId);

            transactions.clear();
            if (allDbTransactions != null && !allDbTransactions.isEmpty()) {
                transactions.addAll(allDbTransactions);
                System.out.println(allDbTransactions.size() + " transações carregadas do usuário " + currentUserId);
            }

            allTransactions = transactions;

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            updateEmptyState();
            updateViewAllButton();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar todas as transações", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateViewAllButton() {
        if (tvViewAll != null) {
            if (showingAllTransactions) {
                tvViewAll.setText("Ver recentes");
            } else {
                tvViewAll.setText("Ver todas (" + allTransactions.size() + ")");
            }
        }
    }

    private void updateEmptyState() {
        LinearLayout layoutEmpty = findViewById(R.id.layoutEmptyTransactions);
        RecyclerView rvTransactions = findViewById(R.id.rvTransactions);

        if (layoutEmpty != null && rvTransactions != null) {
            if (transactions.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvTransactions.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupFinancialData() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : allTransactions) {
            if ("Receita".equals(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("Despesa".equals(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (tvIncome != null) {
            tvIncome.setText(format.format(totalIncome));
        }
        if (tvExpense != null) {
            tvExpense.setText(format.format(totalExpense));
        }
        if (tvBalance != null) {
            tvBalance.setText(format.format(balance));
        }

        if (tvBalance != null) {
            tvBalance.setTextColor(getResources().getColor(R.color.black));
        }

        if (tvBalanceChange != null && ivTrending != null) {
            if (balance > 0) {
                tvBalanceChange.setText("Saldo positivo");
                tvBalanceChange.setTextColor(getResources().getColor(R.color.success_green));
                ivTrending.setImageResource(R.drawable.ic_trending_up);
                ivTrending.setColorFilter(getResources().getColor(R.color.success_green));
            } else if (balance < 0) {
                tvBalanceChange.setText("Saldo negativo");
                tvBalanceChange.setTextColor(getResources().getColor(R.color.error_red));
                ivTrending.setImageResource(R.drawable.ic_trending_down);
                ivTrending.setColorFilter(getResources().getColor(R.color.error_red));
            } else {
                tvBalanceChange.setText("Saldo zerado");
                tvBalanceChange.setTextColor(getResources().getColor(R.color.text_secondary));
                ivTrending.setImageResource(R.drawable.ic_trending_neutral);
                ivTrending.setColorFilter(getResources().getColor(R.color.text_secondary));
            }
        }
    }

    private void setupRecyclerView() {
        if (rvTransactions != null) {
            adapter = new TransactionAdapter(transactions);
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTransactionsView();
                }
            });
        }

        ImageView ivEmptyTransaction = findViewById(R.id.ivEmptyTransaction);
        if (ivEmptyTransaction != null) {
            ivEmptyTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddTransactionActivity();
                }
            });
        }
    }

    private void toggleTransactionsView() {
        if (showingAllTransactions) {
            showingAllTransactions = false;
            loadRecentTransactions();
            showMessage("Mostrando transações recentes");
        } else {
            showingAllTransactions = true;
            loadAllTransactions();
            showMessage("Mostrando todas as transações");
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                return true;
            } else if (itemId == R.id.menu_transactions) {
                openAddTransactionActivity();
                return true;
            } else if (itemId == R.id.menu_reports) {
                navigateToRelatorios();
                return true;
            } else if (itemId == R.id.menu_profile) {
                navigateToProfile();
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.menu_dashboard);
    }

    private void openAddTransactionActivity() {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        startActivity(intent);
    }

    private void addMoreTransactions() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            String today = dateFormat.format(new Date());

            Transaction uber = new Transaction(currentUserId, "Uber", "Transporte", 25.50, "Despesa", today, "Viagem para trabalho");
            Transaction cinema = new Transaction(currentUserId, "Cinema", "Lazer", 45.00, "Despesa", today, "Fim de semana");
            Transaction bonus = new Transaction(currentUserId, "Bônus", "Trabalho", 300.00, "Receita", today, "Bônus de produtividade");

            transactionDao.insert(uber);
            transactionDao.insert(cinema);
            transactionDao.insert(bonus);

            if (showingAllTransactions) {
                loadAllTransactions();
            } else {
                loadRecentTransactions();
            }

            showMessage("Mais transações adicionadas!");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Erro ao adicionar transações");
        }
    }

    private void navigateToRelatorios() {
        Intent intent = new Intent(this, RelatoriosActivity.class);
        startActivity(intent);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showingAllTransactions) {
            loadAllTransactions();
        } else {
            loadRecentTransactions();
        }

        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.menu_dashboard);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}