package com.example.finalproject.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.models.CalendarDay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarTapeAdapter extends RecyclerView.Adapter<CalendarTapeAdapter.CalendarViewHolder> {

    private final List<CalendarDay> days;
    private final OnDayClickListener listener;
    private int selectedDay = -1;
    private Map<Integer, Integer> tapeColorsByDay = new HashMap<>();

    public interface OnDayClickListener {
        void onClick(int day);
    }

    public CalendarTapeAdapter(List<CalendarDay> days, int initialSelectedDay, OnDayClickListener listener) {
        this.days = days;
        this.selectedDay = initialSelectedDay;
        this.listener = listener;
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public void setTapeColors(Map<Integer, Integer> colors) {
        this.tapeColorsByDay = colors != null ? colors : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_tape, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        if (!day.isValid) {
            holder.itemView.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.tvDayNumber.setText(String.valueOf(day.day));

        boolean isSelected = (day.day == selectedDay);
        boolean hasTape = tapeColorsByDay != null && tapeColorsByDay.containsKey(day.day);

        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        int cornerRadius = (int) (8 * density);

        Context context = holder.itemView.getContext();
        int defaultShellColor = ContextCompat.getColor(context, R.color.paper_cream);
        int defaultStrokeColor = ContextCompat.getColor(context, R.color.card_stroke);
        int defaultTextColor = ContextCompat.getColor(context, R.color.text_primary);
        int selectedTextColor = ContextCompat.getColor(context, R.color.fab_wax_red);

        // 1. CASSETTE SHELL COLOR (CHANGES ON DAYS WITH RECORDED TAPES)
        GradientDrawable shellDrawable = new GradientDrawable();
        shellDrawable.setCornerRadius(cornerRadius);

        if (hasTape) {
            Integer tapeColor = tapeColorsByDay.get(day.day);
            int color = tapeColor != null ? tapeColor : 0xFFEEC373;
            shellDrawable.setColor(color);
            shellDrawable.setStroke((int) (1.5f * density), 0x332C211B);

            GradientDrawable ribbonDrawable = new GradientDrawable();
            ribbonDrawable.setCornerRadius(2 * density);
            ribbonDrawable.setColor(0x882C211B);
            holder.viewTapeDot.setBackground(ribbonDrawable);
            holder.viewTapeDot.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setTextColor(0xFF2C211B);
        } else {
            // Theme-adaptive blank cassette shell
            shellDrawable.setColor(defaultShellColor);
            shellDrawable.setStroke((int) (1f * density), defaultStrokeColor);
            holder.viewTapeDot.setVisibility(View.GONE);
            holder.tvDayNumber.setTextColor(defaultTextColor);
        }
        holder.viewTapeShell.setBackground(shellDrawable);

        // 2. SELECTED DAY BORDER HIGHLIGHT
        if (isSelected) {
            holder.viewSelectedBorder.setVisibility(View.VISIBLE);
            if (!hasTape) {
                holder.tvDayNumber.setTextColor(selectedTextColor);
            }
        } else {
            holder.viewSelectedBorder.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedDay = day.day;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onClick(day.day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {

        View viewTapeShell;
        View viewSelectedBorder;
        View viewTapeDot;
        TextView tvDayNumber;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            viewTapeShell = itemView.findViewById(R.id.viewTapeShell);
            viewSelectedBorder = itemView.findViewById(R.id.viewSelectedBorder);
            viewTapeDot = itemView.findViewById(R.id.viewTapeDot);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
