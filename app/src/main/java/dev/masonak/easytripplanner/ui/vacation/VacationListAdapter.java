package dev.masonak.easytripplanner.ui.vacation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.masonak.easytripplanner.R;
import dev.masonak.easytripplanner.data.entity.Vacation;

public class VacationListAdapter extends RecyclerView.Adapter<VacationListAdapter.VacationViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Vacation vacation);
    }

    private List<Vacation> vacations;
    private OnItemClickListener listener;

    public VacationListAdapter(List<Vacation> vacations, OnItemClickListener listener) {
        this.vacations = vacations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vacation, parent, false);
        return new VacationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        final Vacation vacation = vacations.get(position);
        holder.textViewName.setText(vacation.getTitle());

        String details = "";
        if (vacation.getAccommodation() != null && !vacation.getAccommodation().isEmpty()) {
            details += vacation.getAccommodation();
        }
        if (vacation.getStartDate() != null && !vacation.getStartDate().isEmpty()
                && vacation.getEndDate() != null && !vacation.getEndDate().isEmpty()) {
            if (!details.isEmpty()) {
                details += " - ";
            }
            details += vacation.getStartDate() + " to " + vacation.getEndDate();
        }
        holder.textViewDetails.setText(details);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(vacation);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return vacations.size();
    }

    public void setVacations(List<Vacation> vacations) {
        this.vacations = vacations;
        notifyDataSetChanged();
    }

    static class VacationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDetails;

        VacationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewVacationName);
            textViewDetails = itemView.findViewById(R.id.textViewVacationDetails);
        }
    }

}