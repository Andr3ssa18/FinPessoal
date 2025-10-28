package com.example.finpessoal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etAmount, etDate, etNotes;
    private RadioGroup radioGroupType;
    private RadioButton radioIncome, radioExpense;
    private Spinner spinnerCategory;
    private MaterialButton btnSave, btnCancel, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Configurar o comportamento do botão voltar
        setupBackPressedCallback();

        initializeViews();
        setupSpinners();
        setupClickListeners();
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
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        // Configurar data atual
        setupCurrentDate();
    }

    private void setupCurrentDate() {
        String currentDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        etDate.setText(currentDate);
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelAndReturn();
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAndReturn();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAndReturn();
            }
        });

        // Configurar clique no campo de data
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        // Implementar DatePickerDialog aqui
        // Por enquanto, vamos apenas mostrar uma mensagem
        android.widget.Toast.makeText(this, "Seletor de data será implementado", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validar tipo de transação
        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
        if (selectedTypeId == -1) {
            showError("Selecione o tipo de transação");
            return;
        }

        String type = (selectedTypeId == R.id.radioIncome) ? "Receita" : "Despesa";

        // Validações
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

        // Se for despesa, converter para negativo
        if (type.equals("Despesa")) {
            amount = -amount;
        }

        // Criar intent com os dados
        Intent resultIntent = new Intent();
        resultIntent.putExtra("TRANSACTION_TITLE", title);
        resultIntent.putExtra("TRANSACTION_AMOUNT", amount);
        resultIntent.putExtra("TRANSACTION_TYPE", type);
        resultIntent.putExtra("TRANSACTION_CATEGORY", category);
        resultIntent.putExtra("TRANSACTION_DATE", date);
        resultIntent.putExtra("TRANSACTION_NOTES", notes);

        setResult(RESULT_OK, resultIntent);

        showSuccess("Transação adicionada com sucesso!");
        finish();
    }

    private void cancelAndReturn() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void showError(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}