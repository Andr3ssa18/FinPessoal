package com.example.finpessoal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finpessoal.entities.Transaction;

import java.text.NumberFormat;
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
        private TextView tvTitle, tvCategory, tvAmount, tvDate, tvNotes;
        private ImageView ivNotes;
        private TextView fallbackTextView;
        private boolean notesExpanded = false;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView instanceof TextView) {
                fallbackTextView = (TextView) itemView;
            } else {
                try {
                    tvTitle = itemView.findViewById(R.id.tvTitle);
                    tvCategory = itemView.findViewById(R.id.tvCategory);
                    tvAmount = itemView.findViewById(R.id.tvAmount);
                    tvDate = itemView.findViewById(R.id.tvDate);
                    tvNotes = itemView.findViewById(R.id.tvNotes);
                    ivNotes = itemView.findViewById(R.id.ivNotes);
                } catch (Exception e) {
                    fallbackTextView = new TextView(itemView.getContext());
                }
            }
        }

        public void bind(Transaction transaction) {
            if (fallbackTextView != null) {
                String info = transaction.getTitle() + " - " +
                        NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                                .format(transaction.getAmount());
                fallbackTextView.setText(info);
            } else {
                tvTitle.setText(transaction.getTitle());

                if ("Despesa".equals(transaction.getType()) &&
                        transaction.getCategory() != null &&
                        !transaction.getCategory().isEmpty()) {
                    tvCategory.setText(transaction.getCategory());
                } else {
                    tvCategory.setText(transaction.getType());
                }

                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                String amount = format.format(transaction.getAmount());
                tvAmount.setText(amount);

                if ("Receita".equals(transaction.getType())) {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.holo_green_light));
                } else {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.holo_red_light));
                }

                tvDate.setText(transaction.getDate());

                if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
                    ivNotes.setVisibility(View.VISIBLE);
                    tvNotes.setText(transaction.getNotes());
                    notesExpanded = false;
                    tvNotes.setVisibility(View.GONE);
                    ivNotes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notesExpanded = !notesExpanded;
                            if (notesExpanded) {
                                tvNotes.setVisibility(View.VISIBLE);
                                ivNotes.setColorFilter(itemView.getContext().getResources().getColor(R.color.primaryYellow));
                            } else {
                                tvNotes.setVisibility(View.GONE);
                                ivNotes.setColorFilter(itemView.getContext().getResources().getColor(R.color.text_secondary));
                            }
                        }
                    });
                } else {
                    ivNotes.setVisibility(View.GONE);
                    tvNotes.setVisibility(View.GONE);
                }
            }
        }
    }
}