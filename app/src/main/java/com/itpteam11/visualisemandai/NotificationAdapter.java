package com.itpteam11.visualisemandai;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This custom RecyclerView adapter will create and hold multiple notification
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder>  {
    private List<Message> notificationList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message, sender, timestamp;

        public MyViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_message);
            sender = (TextView) view.findViewById(R.id.notification_list_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_timestamp);
        }
    }

    public NotificationAdapter(List<Message> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list, parent, false);

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message notification = notificationList.get(position);
        holder.message.setText(notification.getMessage());
        holder.sender.setText(notification.getSender());
        holder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
