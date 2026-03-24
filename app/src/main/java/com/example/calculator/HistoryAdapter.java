package com.example.calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryEntry> entries = new ArrayList<>();

    public void addEntry(HistoryEntry entry) {
        entries.add(0, entry);
        notifyItemInserted(0);
    }

    public List<HistoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void setEntries(List<HistoryEntry> list) {
        entries.clear();
        entries.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry entry = entries.get(position);
        holder.tvExpression.setText(entry.expression);
        holder.tvResult.setText(entry.result);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpression;
        TextView tvResult;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvHistoryExpression);
            tvResult = itemView.findViewById(R.id.tvHistoryResult);
        }
    }
}
