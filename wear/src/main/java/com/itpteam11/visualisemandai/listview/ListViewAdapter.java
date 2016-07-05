package com.itpteam11.visualisemandai.listview;

import android.app.Activity;
import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.ViewGroup;

import com.itpteam11.visualisemandai.TramActivity;

import java.util.List;

public class ListViewAdapter extends WearableListView.Adapter  {

    private Context context;
    private List<ListViewItem> listViewItems;

    public ListViewAdapter(Context context, List<ListViewItem> listViewItems) {
        this.context = context;
        this.listViewItems = listViewItems;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (this.context.getClass().equals(TramActivity.class)) {
            Log.d("onCreateViewHolder", "Class name: " + this.context.getClass().toString() + this.getClass().toString());
            Log.d("onCreateViewHolder", "tram");
            return new WearableListView.ViewHolder(new ListViewImageRowView(context));
        }
        else{
            Log.d("onCreateViewHolder", "not tram");
            return new WearableListView.ViewHolder(new ListViewRowView(context));
        }
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {

        if (this.context.getClass().equals(TramActivity.class)) {
            Log.d("onBindViewHolder", "tram");
            ListViewImageRowView listViewImageRowView = (ListViewImageRowView) viewHolder.itemView;
            final ListViewItem listViewImageItem = listViewItems.get(i);
            listViewImageRowView.getImage().setImageResource(listViewImageItem.imageRes);
        } else {
            Log.d("onBindViewHolder", "not tram");
            ListViewRowView listViewRowView = (ListViewRowView) viewHolder.itemView;
            final ListViewItem listViewItem = listViewItems.get(i);
            listViewRowView.getImage().setImageResource(listViewItem.imageRes);
            listViewRowView.getText().setText(listViewItem.text);
        }

    }

    @Override
    public int getItemCount() {
        return listViewItems.size();
    }
}
