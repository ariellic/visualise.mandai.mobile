package com.itpteam11.visualisemandai;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

/**
 * This custom RecyclerView adapter will create and hold different customised CardView
 */
public class CustomCardAdapter extends RecyclerView.Adapter<CustomCardAdapter.ViewHolder> {
    private String[] cardContent;
    private Integer[] cardType;
    private String userID;

    private Context context;

    //Constructor to initiate card content and type
    public CustomCardAdapter(HashMap<Integer, String> dataSet, String userID) {
        cardContent = dataSet.values().toArray(new String[0]);
        cardType = dataSet.keySet().toArray(new Integer[0]);
        this.userID = userID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        context = viewGroup.getContext();

        //Create respective CardView layout from given card type
        switch(viewType) {
            case CardType.CHECK_SHOWTIME:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_check_showtime, viewGroup, false);
                return new CheckShowtimeViewHolder(v);
            case CardType.STAFF_STATUS:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_staff_count, viewGroup, false);
                return new StaffStatusViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Assign card content to respective CardView
        switch(viewHolder.getItemViewType()) {
            case CardType.STAFF_STATUS:
                StaffStatusViewHolder staffStatusHolder = (StaffStatusViewHolder) viewHolder;
                staffStatusHolder.setContent(cardContent[position]);
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

    //Super CardView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    //Staff Status CardView
    private class StaffStatusViewHolder extends ViewHolder {
        private TextView working, onBreak;

        public StaffStatusViewHolder(View v) {
            super(v);
            this.working = (TextView) v.findViewById(R.id.staff_count_card_working_value);
            this.onBreak = (TextView) v.findViewById(R.id.staff_count_card_break_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            working.setText(value[0].equals("")?"0":value[0]);
            onBreak.setText(value[1].equals("")?"0":value[1]);
        }
    }

    //Showtime CardView
    private class CheckShowtimeViewHolder extends ViewHolder {
        private Button checkButton;

        public CheckShowtimeViewHolder(View v) {
            super(v);
            //Add onClickListener to check showtime button to run CheckShowtimeService
            this.checkButton = (Button) v.findViewById(R.id.check_showtime_card_button_check);
            this.checkButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), CheckShowtimeService.class);
                    intent.putExtra(CheckShowtimeService.USER_ID, userID);
                    v.getContext().startService(intent);
                }
            });
        }
    }
}
