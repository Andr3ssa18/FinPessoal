package com.example.finpessoal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RelatoriosActivity extends AppCompatActivity {

    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldo, tvMaiorDespesa, tvMaiorReceita;
    private TextView tvTransactionsCount, tvPeriodo, tvSaldoStatus;
    private LinearLayout layoutStats;
    private MaterialButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        // Configurar o comportamento do botão voltar
        setupBackPressedCallback();

        initializeViews();
        setupRelatorios();
        setupClickListeners();
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initializeViews() {
        tvTotalReceitas = findViewById(R.id.tvTotalReceitas);
        tvTotalDespesas = findViewById(R.id.tvTotalDespesas);
        tvSaldo = findViewById(R.id.tvSaldo);
        tvMaiorDespesa = findViewById(R.id.tvMaiorDespesa);
        tvMaiorReceita = findViewById(R.id.tvMaiorReceita);
        tvTransactionsCount = findViewById(R.id.tvTransactionsCount);
        tvPeriodo = findViewById(R.id.tvPeriodo);
        tvSaldoStatus = findViewById(R.id.tvSaldoStatus);
        layoutStats = findViewById(R.id.layoutStats);

        btnBack = findViewById(R.id.btnBack);
        if (btnBack == null) {
            createBackButton();
        }
    }

    private void createBackButton() {
        btnBack = new MaterialButton(this);
        btnBack.setText("↩️ Voltar para Dashboard");
        btnBack.setTextSize(16);
        btnBack.setBackgroundColor(ContextCompat.getColor(this, R.color.surface));
        btnBack.setTextColor(ContextCompat.getColor(this, R.color.primaryYellow));
        btnBack.setStrokeColorResource(R.color.primaryYellow);
        btnBack.setStrokeWidth(2);
        btnBack.setCornerRadius(12);
        btnBack.setPadding(32, 12, 32, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(32, 0, 32, 16);
        btnBack.setLayoutParams(params);

        // Adicionar ao layout principal
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            mainLayout.addView(btnBack, 0); // Adicionar no topo
        } else {
            // Fallback: adicionar ao root view
            LinearLayout rootView = (LinearLayout) findViewById(android.R.id.content);
            if (rootView != null && rootView.getChildAt(0) instanceof LinearLayout) {
                ((LinearLayout) rootView.getChildAt(0)).addView(btnBack, 0);
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRelatorios() {
        Intent intent = getIntent();
        double totalReceitas = intent.getDoubleExtra("TOTAL_INCOME", 0);
        double totalDespesas = intent.getDoubleExtra("TOTAL_EXPENSE", 0);
        double largestIncome = intent.getDoubleExtra("LARGEST_INCOME", 0);
        double largestExpense = intent.getDoubleExtra("LARGEST_EXPENSE", 0);
        String largestIncomeTitle = intent.getStringExtra("LARGEST_INCOME_TITLE");
        String largestExpenseTitle = intent.getStringExtra("LARGEST_EXPENSE_TITLE");
        int transactionsCount = intent.getIntExtra("TRANSACTIONS_COUNT", 0);

        double saldo = totalReceitas - totalDespesas;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Atualizar os textos
        tvTotalReceitas.setText(format.format(totalReceitas));
        tvTotalDespesas.setText(format.format(totalDespesas));
        tvSaldo.setText(format.format(saldo));

        if (largestIncome > 0) {
            tvMaiorReceita.setText(String.format("%s - %s",
                    largestIncomeTitle != null ? largestIncomeTitle : "Receita",
                    format.format(largestIncome)));
        } else {
            tvMaiorReceita.setText("Nenhuma receita registrada");
        }

        if (largestExpense > 0) {
            tvMaiorDespesa.setText(String.format("%s - %s",
                    largestExpenseTitle != null ? largestExpenseTitle : "Despesa",
                    format.format(largestExpense)));
        } else {
            tvMaiorDespesa.setText("Nenhuma despesa registrada");
        }

        tvTransactionsCount.setText(String.format("Total de transações: %d", transactionsCount));

        String periodo = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR")).format(new Date());
        tvPeriodo.setText(periodo);

        // Configurar cores baseadas no saldo
        if (saldo > 0) {
            tvSaldoStatus.setText("Superávit");
            tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
            tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
        } else if (saldo < 0) {
            tvSaldoStatus.setText("Déficit");
            tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));
            tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));
        } else {
            tvSaldoStatus.setText("Equilíbrio");
            tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.holo_blue_light));
            tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.holo_blue_light));
        }
    }
}