package com.itpteam11.visualisemandai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import com.itpteam11.visualisemandai.listview.ListViewAdapter;
import com.itpteam11.visualisemandai.listview.ListViewItem;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends Activity implements WearableListView.ClickListener {

    private List<ListViewItem> viewItemList = new ArrayList<>();

    TextView mHeader;
    String header;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Bundle bundle = this.getIntent().getExtras();
        header = bundle.getString("header");
        mHeader = (TextView)findViewById(R.id.textView);
        mHeader.setText(header);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.wearable_list_view);

        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Splash Safari"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Elephant Show"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Animal Friends"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Rainforest Fights Back"));

        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
      //  Toast.makeText(this, "Click on " + viewItemList.get(viewHolder.getLayoutPosition()).text, Toast.LENGTH_SHORT).show();
        Bundle b = new Bundle();
        b.putString("header",viewItemList.get(viewHolder.getLayoutPosition()).text);
        Intent intent = new Intent(this, Show_StatusActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
