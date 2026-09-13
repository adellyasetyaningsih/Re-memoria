package com.example.finalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.models.Tape;

import java.util.List;

public class TapeShelfAdapter extends RecyclerView.Adapter<TapeShelfAdapter.TapeViewHolder> {

    public interface OnTapeClick {
        void onClick(Tape tape);
    }

    public interface OnMoreClick {
        void onMore(Tape tape);
    }

    private final List<Tape> tapeList;
    private final OnTapeClick clickListener;
    private final OnMoreClick moreListener;

    public TapeShelfAdapter(List<Tape> tapeList,
                            OnTapeClick clickListener,
                            OnMoreClick moreListener) {
        this.tapeList = tapeList;
        this.clickListener = clickListener;
        this.moreListener = moreListener;
    }

    @NonNull
    @Override
    public TapeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tape_shelf, parent, false);
        return new TapeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TapeViewHolder holder, int position) {
        Tape tape = tapeList.get(position);

        holder.tvTitle.setText(tape.title != null ? tape.title : "Untitled Memory");
        holder.tvDate.setText(tape.date != null ? tape.date : "");

        if (holder.panelColor instanceof CardView) {
            ((CardView) holder.panelColor).setCardBackgroundColor(tape.color);
        } else {
            holder.panelColor.setBackgroundColor(tape.color);
        }

        boolean isDarkTape = isColorDark(tape.color);
        int titleColor = isDarkTape ? 0xFFFFFFFF : 0xFF2A1E17;
        int subColor = isDarkTape ? 0xDDFFFFFF : 0xFF5E4E42;
        holder.tvTitle.setTextColor(titleColor);
        holder.tvDate.setTextColor(subColor);
        holder.btnMore.setColorFilter(titleColor);

        // Tap cassette to play in mini player
        holder.itemView.setOnClickListener(v -> clickListener.onClick(tape));

        // Tap more button to open detail
        holder.btnMore.setOnClickListener(v -> moreListener.onMore(tape));
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255;
        return darkness >= 0.45;
    }

    @Override
    public int getItemCount() {
        return tapeList.size();
    }

    public static class TapeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate;
        View panelColor;
        ImageView btnMore;

        public TapeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTapeTitle);
            tvDate = itemView.findViewById(R.id.tvTapeDate);
            panelColor = itemView.findViewById(R.id.panelColor);
            btnMore = itemView.findViewById(R.id.btnTapeMore);
        }
    }
}
