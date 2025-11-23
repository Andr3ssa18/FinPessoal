package com.example.finpessoal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.finpessoal.database.AppDatabase;
import com.example.finpessoal.dao.TransactionDao;
import com.example.finpessoal.entities.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etAmount, etDate, etNotes;
    private RadioGroup radioGroupType;
    private RadioButton radioIncome, radioExpense;
    private Spinner spinnerCategory;
    private MaterialButton btnSave;
    private BottomNavigationView bottomNavigation;
    private LinearLayout layoutImportance;
    private Calendar selectedCalendar;

    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        currentUserId = sessionManager.getUserId();

        setupBackPressedCallback();

        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNotes = findViewById(R.id.etNotes);
        radioGroupType = findViewById(R.id.radioGroupType);
        radioIncome = findViewById(R.id.radioIncome);
        radioExpense = findViewById(R.id.radioExpense);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        layoutImportance = findViewById(R.id.layoutImportance);

        selectedCalendar = Calendar.getInstance();

        setupCurrentDate();

        radioIncome.setChecked(true);
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

        bottomNavigation.setSelectedItemId(R.id.menu_transactions);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRelatorios() {
        Intent intent = new Intent(this, RelatoriosActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        String currentDate = dateFormat.format(selectedCalendar.getTime());
        etDate.setText(currentDate);
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setSelection(0);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateImportanceVisibility();
            }
        });
    }

    private void updateImportanceVisibility() {
        if (radioExpense.isChecked()) {
            layoutImportance.setVisibility(View.VISIBLE);
        } else {
            layoutImportance.setVisibility(View.GONE);
            spinnerCategory.setSelection(0);
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                    String selectedDate = dateFormat.format(selectedCalendar.getTime());
                    etDate.setText(selectedDate);
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String category = "";

        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
        if (selectedTypeId == -1) {
            Toast.makeText(this, "Selecione o tipo de transação", Toast.LENGTH_SHORT).show();
            return;
        }

        String transactionType = (selectedTypeId == R.id.radioIncome) ? "Receita" : "Despesa";

        if (title.isEmpty()) {
            showError("Digite uma descrição");
            return;
        }

        if (amountStr.isEmpty()) {
            showError("Digite um valor");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showError("Digite um valor maior que zero");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Digite um valor válido");
            return;
        }

        if (date.isEmpty()) {
            showError("Selecione uma data");
            return;
        }

        if (transactionType.equals("Despesa")) {
            category = spinnerCategory.getSelectedItem().toString();
            if ("Selecione a importância".equals(category)) {
                showError("Selecione a importância da despesa");
                return;
            }
        }

        try {
            AppDatabase db = AppDatabase.getInstance(this);
            TransactionDao transactionDao = db.transactionDao();

            Transaction transaction = new Transaction(currentUserId, title, category, amount, transactionType, date, notes);

            long transactionId = transactionDao.insert(transaction);

            if (transactionId > 0) {
                Toast.makeText(this, "Transação salva com sucesso!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                showError("Erro ao salvar transação");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao salvar no banco de dados: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.menu_transactions);
        }
    }
}