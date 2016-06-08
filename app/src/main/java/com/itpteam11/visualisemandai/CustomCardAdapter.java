package com.itpteam11.visualisemandai;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 *
 */
public class CustomCardAdapter extends RecyclerView.Adapter<CustomCardAdapter.ViewHolder> {
    private String[] cardContent;
    private Integer[] cardType;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class StaffAvailableViewHolder extends ViewHolder {
        private TextView content;

        public StaffAvailableViewHolder(View v) {
            super(v);
            this.content = (TextView) v.findViewById(R.id.staff_available_card_textview_content);
        }

        public void setContent(String text) {
            this.content.setText(text);
        }
    }

    public CustomCardAdapter(HashMap<Integer, String> dataSet) {
        cardContent = dataSet.values().toArray(new String[0]);
        cardType = dataSet.keySet().toArray(new Integer[0]);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        switch(viewType) {
            case CardType.STAFF_AVAILABLE:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.staff_available_card, viewGroup, false);
                return new StaffAvailableViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        switch(viewHolder.getItemViewType()) {
            case CardType.STAFF_AVAILABLE:
                StaffAvailableViewHolder holder = (StaffAvailableViewHolder) viewHolder;
                holder.setContent(cardContent[position]);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cardContent.length;
    }

    @Override
    public int getItemViewType(int position) {
        return cardType[position];
    }
}
