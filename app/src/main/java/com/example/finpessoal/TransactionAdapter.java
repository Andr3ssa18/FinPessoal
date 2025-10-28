package com.example.finpessoal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        } catch (Exception e) {
            // Fallback: criar um layout básico programaticamente
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            return new TransactionViewHolder(textView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvCategory, tvAmount, tvDate;
        private TextView fallbackTextView; // Para o fallback

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Verifica se é o layout normal ou fallback
            if (itemView instanceof TextView) {
                fallbackTextView = (TextView) itemView;
            } else {
                try {
                    tvTitle = itemView.findViewById(R.id.tvTitle);
                    tvCategory = itemView.findViewById(R.id.tvCategory);
                    tvAmount = itemView.findViewById(R.id.tvAmount);
                    tvDate = itemView.findViewById(R.id.tvDate);
                } catch (Exception e) {
                    // Se houver erro ao encontrar as views, usar fallback
                    fallbackTextView = new TextView(itemView.getContext());
                }
            }
        }

        public void bind(Transaction transaction) {
            if (fallbackTextView != null) {
                // Modo fallback - mostrar informações básicas
                String info = transaction.getTitle() + " - " +
                        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                                .format(transaction.getAmount());
                fallbackTextView.setText(info);
            } else {
                // Modo normal
                tvTitle.setText(transaction.getTitle());
                tvCategory.setText(transaction.getCategory());

                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                String amount = format.format(transaction.getAmount());
                tvAmount.setText(amount);

                // Definir cor baseada no valor
                if (transaction.getAmount() > 0) {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.holo_green_light));
                } else {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.holo_red_light));
                }

                // Formatar data
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                String formattedDate = dateFormat.format(transaction.getDate());
                tvDate.setText(formattedDate);
            }
        }
    }
}