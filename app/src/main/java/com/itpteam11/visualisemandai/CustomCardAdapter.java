package com.itpteam11.visualisemandai;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            case CardType.STAFF_COUNT:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_staff_count, viewGroup, false);
                return new StaffCountViewHolder(v);
            case CardType.STAFF_STATUS:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_staff_status, viewGroup, false);
                return new StaffStatusViewHolder(v);
            case CardType.SHOWTIME:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_showtime_status, viewGroup, false);
                return new ShowtimeStatusViewHolder(v);
            case CardType.TRAM_STATION:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_tram_station_crowd, viewGroup, false);
                return new TramStationViewHolder(v);
            case CardType.WEATHER:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_weather, viewGroup, false);
                return new WeatherViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Assign card content to respective CardView
        switch(viewHolder.getItemViewType()) {
            case CardType.STAFF_COUNT:
                StaffCountViewHolder staffCountHolder = (StaffCountViewHolder) viewHolder;
                staffCountHolder.setContent(cardContent[position]);
                break;
            case CardType.STAFF_STATUS:
                StaffStatusViewHolder staffStatusViewHolder = (StaffStatusViewHolder) viewHolder;
                staffStatusViewHolder.setContent(cardContent[position]);
                break;
            case CardType.SHOWTIME:
                ShowtimeStatusViewHolder showtimeStatusViewHolder = (ShowtimeStatusViewHolder) viewHolder;
                showtimeStatusViewHolder.setContent(cardContent[position]);
                break;
            case CardType.TRAM_STATION:
                TramStationViewHolder tramStationViewHolder = (TramStationViewHolder) viewHolder;
                tramStationViewHolder.setContent(cardContent[position]);
                break;
            case CardType.WEATHER:
                WeatherViewHolder weatherViewHolder = (WeatherViewHolder) viewHolder;
                weatherViewHolder.setContent(cardContent[position]);
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

    //Staff Count CardView
    private class StaffCountViewHolder extends ViewHolder {
        private TextView working, onBreak;

        public StaffCountViewHolder(View v) {
            super(v);
            this.working = (TextView) v.findViewById(R.id.staff_count_card_working_value);
            this.onBreak = (TextView) v.findViewById(R.id.staff_count_card_break_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            working.setText(value[0]);
            onBreak.setText(value[1]);
        }
    }

    //Staff Status CardView
    private class StaffStatusViewHolder extends ViewHolder {
        private TextView status;

        public StaffStatusViewHolder(View v) {
            super(v);
            this.status = (TextView) v.findViewById(R.id.staff_status_card_work_status_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            status.setText(value[0]);
        }
    }

    //Showtime Status CardView
    private class ShowtimeStatusViewHolder extends ViewHolder {
        private TextView splashSafari, animalFriends, elephantAtWorkNPlay, rainforestFightsBack;

        public ShowtimeStatusViewHolder(View v) {
            super(v);
            this.splashSafari = (TextView) v.findViewById(R.id.showtime_status_card_show1_value);
            this.animalFriends = (TextView) v.findViewById(R.id.showtime_status_card_show2_value);
            this.elephantAtWorkNPlay = (TextView) v.findViewById(R.id.showtime_status_card_show3_value);
            this.rainforestFightsBack = (TextView) v.findViewById(R.id.showtime_status_card_show4_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            splashSafari.setText(value[0]);
            animalFriends.setText(value[1]);
            elephantAtWorkNPlay.setText(value[2]);
            rainforestFightsBack.setText(value[3]);
        }
    }

    //Showtime Status CardView
    private class TramStationViewHolder extends ViewHolder {
        private TextView station1, station2, station3, station4;

        public TramStationViewHolder(View v) {
            super(v);
            this.station1 = (TextView) v.findViewById(R.id.tram_station_crowd_card_station1_value);
            this.station2 = (TextView) v.findViewById(R.id.tram_station_crowd_card_station2_value);
            this.station3 = (TextView) v.findViewById(R.id.tram_station_crowd_card_station3_value);
            this.station4 = (TextView) v.findViewById(R.id.tram_station_crowd_card_station4_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            station1.setText(value[0]);
            station2.setText(value[1]);
            station3.setText(value[2]);
            station4.setText(value[3]);
        }
    }

    //Showtime Status CardView
    private class WeatherViewHolder extends ViewHolder {
        private TextView weather, temperature, psi;

        public WeatherViewHolder(View v) {
            super(v);
            this.weather = (TextView) v.findViewById(R.id.weather_card_weather_value);
            this.temperature = (TextView) v.findViewById(R.id.weather_card_temperature_value);
            this.psi = (TextView) v.findViewById(R.id.weather_card_psi_value);
        }

        public void setContent(String text) {
            String[] value = text.split("-");
            Log.d("SETCONTENTWEATHER", value.toString());
            weather.setText(value[0]);
            temperature.setText(value[1]);
            psi.setText(value[2]);
        }
    }
}
