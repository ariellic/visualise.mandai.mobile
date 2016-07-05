package com.itpteam11.visualisemandai;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.itpteam11.visualisemandai.listview.ListViewAdapter;
import com.itpteam11.visualisemandai.listview.ListViewItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    private List<ListViewItem> mainItemList = new ArrayList<>();
    public static String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WearableListView wearableListView = (WearableListView) findViewById(R.id.listview);

        mainItemList.add(new ListViewItem(R.drawable.escape, "Animal Escape"));
        mainItemList.add(new ListViewItem(R.drawable.dine, "Status"));
        mainItemList.add(new ListViewItem(R.drawable.show, "Shows"));
        mainItemList.add(new ListViewItem(R.drawable.tram, "Tram"));


        wearableListView.setAdapter(new ListViewAdapter(this, mainItemList));
        wearableListView.setClickListener(this);

     }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        //  Toast.makeText(this, "Click on " + viewItemList.get(viewHolder.getLayoutPosition()).text, Toast.LENGTH_SHORT).show();
        String title = mainItemList.get(viewHolder.getLayoutPosition()).text;
        Bundle b = new Bundle();
        b.putString("header", title);
        if(title.equals("Status")) {
            Intent intent = new Intent(this, StatusActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
        else if(title.equals("Shows")){
            Intent intent = new Intent(this, ShowActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
        else if(title.equals("Tram")){
            Intent intent = new Intent(this, TramActivity.class);
            b.putString("tramheader", "Tram Stations");
            intent.putExtras(b);
            startActivity(intent);
        }
        else if(title.equals("Animal Escape")){
            Intent intent = new Intent(this, EscapeActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Click on " + mainItemList.get(viewHolder.getLayoutPosition()).text, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onTopEmptyRegionClick() {
        Toast.makeText(this, "You tapped on Top empty area", Toast.LENGTH_SHORT).show();
    }



}
