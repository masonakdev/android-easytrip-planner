package dev.masonak.easytripplanner.ui.excursion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.masonak.easytripplanner.R;
import dev.masonak.easytripplanner.data.entity.Excursion;

public class ExcursionListAdapter extends RecyclerView.Adapter<ExcursionListAdapter.ExcursionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Excursion excursion);
    }

    private List<Excursion> excursions;
    private OnItemClickListener listener;

    public ExcursionListAdapter(List<Excursion> excursions, OnItemClickListener listener) {
        this.excursions = excursions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_excursion, parent, false);
        return new ExcursionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionViewHolder holder, int position) {
        final Excursion excursion = excursions.get(position);
        holder.textViewExcursionTitle.setText(excursion.getTitle());
        holder.textViewExcursionDate.setText(excursion.getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(excursion);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return excursions.size();
    }

    public void setExcursions(List<Excursion> excursions) {
        this.excursions = excursions;
        notifyDataSetChanged();
    }

    static class ExcursionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewExcursionTitle;
        TextView textViewExcursionDate;

        ExcursionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewExcursionTitle = itemView.findViewById(R.id.textViewExcursionTitle);
            textViewExcursionDate = itemView.findViewById(R.id.textViewExcursionDate);
        }
    }

}