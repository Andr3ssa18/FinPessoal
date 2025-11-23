package com.example.finpessoal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.TransactionDao;
import com.example.finpessoal.entities.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RelatoriosActivity extends AppCompatActivity {

    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldo, tvMaiorDespesa, tvMaiorReceita;
    private TextView tvTransactionsCount, tvPeriodo, tvSaldoStatus;
    private TextView tvMetaMensal, tvProgressoMeta, tvProgressoPercentual;
    private TextView tvMetaAnual, tvProgressoMetaAnual, tvProgressoPercentualAnual;
    private BottomNavigationView bottomNavigation;
    private MaterialButton btnDefinirMeta, btnDefinirMetaAnual, btnVerReceita, btnVerDespesa;
    private LinearProgressIndicator progressBarMeta, progressBarMetaAnual;
    private ImageView ivTrending;

    private double metaMensal = 0.0;
    private double metaAnual = 0.0;
    private int currentUserId;
    private AppDatabase db;
    private TransactionDao transactionDao;
    private SharedPreferences preferences;

    private Transaction maiorReceita;
    private Transaction maiorDespesa;

    private static final String PREFS_NAME = "MetasPrefs";
    private static final String KEY_META_MENSAL = "meta_mensal";
    private static final String KEY_META_ANUAL = "meta_anual";
    private static final String KEY_META_INICIALIZADA = "meta_inicializada";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        db = AppDatabase.getInstance(this);
        transactionDao = db.transactionDao();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupBackPressedCallback();

        initializeViews();
        inicializarMetasPrimeiraVez();
        setupRelatorios();
        setupBottomNavigation();
        setupMetaListeners();
        setupTransactionListeners();
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
        tvMetaMensal = findViewById(R.id.tvMetaMensal);
        tvProgressoMeta = findViewById(R.id.tvProgressoMeta);
        tvProgressoPercentual = findViewById(R.id.tvProgressoPercentual);
        tvMetaAnual = findViewById(R.id.tvMetaAnual);
        tvProgressoMetaAnual = findViewById(R.id.tvProgressoMetaAnual);
        tvProgressoPercentualAnual = findViewById(R.id.tvProgressoPercentualAnual);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnDefinirMeta = findViewById(R.id.btnDefinirMeta);
        btnDefinirMetaAnual = findViewById(R.id.btnDefinirMetaAnual);
        btnVerReceita = findViewById(R.id.btnVerReceita);
        btnVerDespesa = findViewById(R.id.btnVerDespesa);
        progressBarMeta = findViewById(R.id.progressBarMeta);
        progressBarMetaAnual = findViewById(R.id.progressBarMetaAnual);
        ivTrending = findViewById(R.id.ivTrending);
    }

    private void setupTransactionListeners() {
        btnVerReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maiorReceita != null) {
                    showTransactionDetails(maiorReceita);
                }
            }
        });

        btnVerDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maiorDespesa != null) {
                    showTransactionDetails(maiorDespesa);
                }
            }
        });
    }

    private void showTransactionDetails(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalhes da Transação");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transaction_details, null);
        builder.setView(dialogView);

        TextView tvDetailTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvDetailCategory = dialogView.findViewById(R.id.tvDetailCategory);
        TextView tvDetailAmount = dialogView.findViewById(R.id.tvDetailAmount);
        TextView tvDetailType = dialogView.findViewById(R.id.tvDetailType);
        TextView tvDetailDate = dialogView.findViewById(R.id.tvDetailDate);
        TextView tvDetailNotes = dialogView.findViewById(R.id.tvDetailNotes);
        View layoutNotes = dialogView.findViewById(R.id.layoutNotes);
        View layoutImportancia = dialogView.findViewById(R.id.layoutImportancia);

        tvDetailTitle.setText(transaction.getTitle());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String amount = format.format(transaction.getAmount());
        tvDetailAmount.setText(amount);

        if ("Receita".equals(transaction.getType())) {
            tvDetailAmount.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
            tvDetailType.setText("Receita");
            tvDetailType.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
            layoutImportancia.setVisibility(View.GONE);
        } else {
            tvDetailAmount.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));
            tvDetailType.setText("Despesa");
            tvDetailType.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));

            if (transaction.getCategory() != null && !transaction.getCategory().isEmpty()) {
                tvDetailCategory.setText(transaction.getCategory());
                layoutImportancia.setVisibility(View.VISIBLE);
            } else {
                layoutImportancia.setVisibility(View.GONE);
            }
        }

        tvDetailDate.setText(transaction.getDate());

        if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
            tvDetailNotes.setText(transaction.getNotes());
            layoutNotes.setVisibility(View.VISIBLE);
        } else {
            layoutNotes.setVisibility(View.GONE);
        }

        builder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inicializarMetasPrimeiraVez() {
        boolean metaInicializada = preferences.getBoolean(KEY_META_INICIALIZADA, false);

        if (!metaInicializada) {
            metaMensal = 0.0;
            metaAnual = 0.0;

            salvarMetaMensal(metaMensal);
            salvarMetaAnual(metaAnual);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_META_INICIALIZADA, true);
            editor.apply();
        } else {
            carregarMetasSalvas();
        }
    }

    private void carregarMetasSalvas() {
        float metaMensalSalva = preferences.getFloat(KEY_META_MENSAL, -1.0f);
        float metaAnualSalva = preferences.getFloat(KEY_META_ANUAL, -1.0f);

        metaMensal = metaMensalSalva == -1.0f ? 0.0 : metaMensalSalva;
        metaAnual = metaAnualSalva == -1.0f ? 0.0 : metaAnualSalva;
    }

    private void salvarMetaMensal(double meta) {
        SharedPreferences.Editor editor = preferences.edit();
        float valorParaSalvar = meta == 0.0 ? -1.0f : (float) meta;
        editor.putFloat(KEY_META_MENSAL, valorParaSalvar);
        editor.apply();
    }

    private void salvarMetaAnual(double meta) {
        SharedPreferences.Editor editor = preferences.edit();
        float valorParaSalvar = meta == 0.0 ? -1.0f : (float) meta;
        editor.putFloat(KEY_META_ANUAL, valorParaSalvar);
        editor.apply();
    }

    private void setupMetaListeners() {
        btnDefinirMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMetaDialog("Mensal", "Digite sua meta mensal (deixe vazio para remover):", metaMensal, new MetaDialogListener() {
                    @Override
                    public void onMetaDefinida(double meta) {
                        metaMensal = meta;
                        salvarMetaMensal(meta);
                        atualizarMetaMensalUI();
                        if (meta > 0) {
                            showSuccess("Meta mensal definida: " + formatarMoeda(meta));
                        } else {
                            showSuccess("Meta mensal removida");
                        }
                    }
                });
            }
        });

        btnDefinirMetaAnual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMetaDialog("Anual", "Digite sua meta anual (deixe vazio para remover):", metaAnual, new MetaDialogListener() {
                    @Override
                    public void onMetaDefinida(double meta) {
                        metaAnual = meta;
                        salvarMetaAnual(meta);
                        atualizarMetaAnualUI();
                        if (meta > 0) {
                            showSuccess("Meta anual definida: " + formatarMoeda(meta));
                        } else {
                            showSuccess("Meta anual removida");
                        }
                    }
                });
            }
        });
    }

    private void showMetaDialog(String tipo, String mensagem, double valorAtual, final MetaDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Definir Meta " + tipo);
        builder.setMessage(mensagem);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Ex: 5000.00");
        if (valorAtual > 0) {
            input.setText(String.format(Locale.getDefault(), "%.2f", valorAtual));
        }
        input.setSelectAllOnFocus(true);
        builder.setView(input);

        builder.setPositiveButton("Definir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String metaStr = input.getText().toString();
                if (!metaStr.isEmpty()) {
                    try {
                        double meta = Double.parseDouble(metaStr);
                        if (meta > 0) {
                            listener.onMetaDefinida(meta);
                        } else {
                            showError("A meta deve ser maior que zero");
                        }
                    } catch (NumberFormatException e) {
                        showError("Digite um valor válido");
                    }
                } else {
                    listener.onMetaDefinida(0.0);
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private interface MetaDialogListener {
        void onMetaDefinida(double meta);
    }

    private void atualizarMetaMensalUI() {
        double saldoAtual = getSaldoAtual();
        atualizarMetaUI(metaMensal, saldoAtual, tvMetaMensal, tvProgressoMeta, tvProgressoPercentual, progressBarMeta, "Mensal");
    }

    private void atualizarMetaAnualUI() {
        double saldoAtual = getSaldoAtual();
        atualizarMetaUI(metaAnual, saldoAtual, tvMetaAnual, tvProgressoMetaAnual, tvProgressoPercentualAnual, progressBarMetaAnual, "Anual");
    }

    private void atualizarMetaUI(double meta, double saldoAtual, TextView tvMeta, TextView tvProgresso,
                                 TextView tvPercentual, LinearProgressIndicator progressBar, String tipo) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (meta > 0) {
            tvMeta.setText(format.format(meta));

            double progresso = Math.min(saldoAtual, meta);
            double percentual = (progresso / meta) * 100;

            tvProgresso.setText(format.format(progresso));
            tvPercentual.setText(String.format(Locale.getDefault(), "%.1f%%", percentual));

            progressBar.setProgress((int) percentual);

            if (percentual >= 100) {
                tvPercentual.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
                progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.holo_green_light));
            } else if (percentual >= 50) {
                tvPercentual.setTextColor(ContextCompat.getColor(this, R.color.primaryYellow));
                progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.primaryYellow));
            } else {
                tvPercentual.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));
                progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.holo_red_light));
            }
        } else {
            tvMeta.setText("Não definida");
            tvProgresso.setText(format.format(saldoAtual));
            tvPercentual.setText("--");
            progressBar.setProgress(0);
            tvPercentual.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            progressBar.setIndicatorColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private double getSaldoAtual() {
        double totalReceitas = transactionDao.getTotalIncome(currentUserId);
        double totalDespesas = transactionDao.getTotalExpenses(currentUserId);
        return totalReceitas - totalDespesas;
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
                return true;
            } else if (itemId == R.id.menu_profile) {
                navigateToProfile();
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.menu_reports);
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

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupRelatorios() {
        try {
            double totalReceitas = transactionDao.getTotalIncome(currentUserId);
            double totalDespesas = transactionDao.getTotalExpenses(currentUserId);
            double saldo = totalReceitas - totalDespesas;

            maiorReceita = transactionDao.getLargestIncome(currentUserId);
            maiorDespesa = transactionDao.getLargestExpense(currentUserId);

            int transactionsCount = transactionDao.getTransactionsCount(currentUserId);

            atualizarUI(totalReceitas, totalDespesas, saldo, maiorReceita, maiorDespesa, transactionsCount);

            atualizarMetaMensalUI();
            atualizarMetaAnualUI();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar relatórios: " + e.getMessage());
        }
    }

    private void atualizarUI(double totalReceitas, double totalDespesas, double saldo,
                             Transaction maiorReceita, Transaction maiorDespesa, int transactionsCount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        tvSaldo.setText(format.format(saldo));

        double totalMovimentacoes = totalReceitas + totalDespesas;
        double porcentagemReceitas = totalMovimentacoes > 0 ? (totalReceitas / totalMovimentacoes) * 100 : 0;
        double porcentagemDespesas = totalMovimentacoes > 0 ? (totalDespesas / totalMovimentacoes) * 100 : 0;

        double mediaReceitasMensal = totalReceitas > 0 ? totalReceitas / 12 : 0;
        double mediaDespesasMensal = totalDespesas > 0 ? totalDespesas / 12 : 0;

        double taxaEconomia = totalReceitas > 0 ? ((totalReceitas - totalDespesas) / totalReceitas) * 100 : 0;

        double proporcaoDespesasReceitas = totalReceitas > 0 ? (totalDespesas / totalReceitas) * 100 : 0;

        String textoReceitas = String.format(Locale.getDefault(),
                "<font color='#FFFFFF'><small><b>Valor Total:</b></small></font><br>" +
                        "<font color='#669900'>%s</font><br>" +
                        "<font color='#FFFFFF'><small><b>Porcentagem do Total:</b></small></font><br>" +
                        "<font color='#669900'>%.1f%%</font><br>" +
                        "<font color='#FFFFFF'><small><b>Média Mensal:</b></small></font><br>" +
                        "<font color='#669900'>%s</font>",
                format.format(totalReceitas),
                porcentagemReceitas,
                format.format(mediaReceitasMensal));

        String textoDespesas = String.format(Locale.getDefault(),
                "<font color='#FFFFFF'><small><b>Valor Total:</b></small></font><br>" +
                        "<font color='#F44336'>%s</font><br>" +
                        "<font color='#FFFFFF'><small><b>Porcentagem do Total:</b></small></font><br>" +
                        "<font color='#F44336'>%.1f%%</font><br>" +
                        "<font color='#FFFFFF'><small><b>Média Mensal:</b></small></font><br>" +
                        "<font color='#F44336'>%s</font><br>" +
                        "<font color='#FFFFFF'><small><b>Proporção das Receitas:</b></small></font><br>" +
                        "<font color='#F44336'>%.1f%%</font>",
                format.format(totalDespesas),
                porcentagemDespesas,
                format.format(mediaDespesasMensal),
                proporcaoDespesasReceitas);

        tvTotalReceitas.setText(android.text.Html.fromHtml(textoReceitas));
        tvTotalDespesas.setText(android.text.Html.fromHtml(textoDespesas));

        if (tvSaldoStatus != null) {
            String statusSaldo;
            if (saldo > 0) {
                statusSaldo = String.format(Locale.getDefault(), "Saldo positivo\nEconomia: %.1f%%", taxaEconomia);
            } else if (saldo < 0) {
                statusSaldo = String.format(Locale.getDefault(), "Saldo negativo\nDéficit: %.1f%%", Math.abs(taxaEconomia));
            } else {
                statusSaldo = "Saldo zerado\nEquilíbrio total";
            }
            tvSaldoStatus.setText(statusSaldo);
        }

        if (maiorReceita != null) {
            String receitaText = String.format("%s\n%s",
                    maiorReceita.getTitle(),
                    format.format(maiorReceita.getAmount()));
            tvMaiorReceita.setText(receitaText);
            tvMaiorReceita.setTextColor(ContextCompat.getColor(this, R.color.holo_green_light));
            btnVerReceita.setVisibility(View.VISIBLE);
        } else {
            tvMaiorReceita.setText("Nenhuma receita\nregistrada");
            tvMaiorReceita.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            btnVerReceita.setVisibility(View.GONE);
        }

        if (maiorDespesa != null) {
            String despesaText = String.format("%s\n%s",
                    maiorDespesa.getTitle(),
                    format.format(maiorDespesa.getAmount()));
            tvMaiorDespesa.setText(despesaText);
            tvMaiorDespesa.setTextColor(ContextCompat.getColor(this, R.color.holo_red_light));
            btnVerDespesa.setVisibility(View.VISIBLE);
        } else {
            tvMaiorDespesa.setText("Nenhuma despesa\nregistrada");
            tvMaiorDespesa.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            btnVerDespesa.setVisibility(View.GONE);
        }

        tvTransactionsCount.setText(String.format(Locale.getDefault(), "%d transações", transactionsCount));

        String periodo = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR")).format(new Date());
        tvPeriodo.setText(periodo);

        if (tvSaldoStatus != null && ivTrending != null) {
            ivTrending.clearColorFilter();

            if (saldo > 0) {
                tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green));
                tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.black));
                ivTrending.setImageResource(R.drawable.ic_trending_up);
            } else if (saldo < 0) {
                tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.error_red));
                tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.black));
                ivTrending.setImageResource(R.drawable.ic_trending_down);
            } else {
                tvSaldoStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.black));
                ivTrending.setImageResource(R.drawable.ic_trending_neutral);
            }
        }
    }

    private String formatarMoeda(double valor) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valor);
    }

    private void showSuccess(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRelatorios();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.menu_reports);
        }
    }
}