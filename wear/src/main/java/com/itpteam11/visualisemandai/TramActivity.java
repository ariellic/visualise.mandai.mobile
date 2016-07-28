package com.itpteam11.visualisemandai;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.itpteam11.visualisemandai.listview.ListViewAdapter;
import com.itpteam11.visualisemandai.listview.ListViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 *  This activity shows all the tram stations' number. When user tapped on a tram station number,
 *  it will direct them to the TramStatusActivity
 */

public class TramActivity extends Activity implements WearableListView.ClickListener {

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    public static String TAG = "TramActivity";

    private List<ListViewItem> viewItemList = new ArrayList<>();

    TextView mHeader;
    String header;

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError=false;
    int send = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tram);

        Bundle bundle = this.getIntent().getExtras();
        header = bundle.getString("tramheader");
        mHeader = (TextView)findViewById(R.id.textView);
        mHeader.setText(header);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.image_list_view);

        viewItemList.add(new ListViewItem(R.drawable.one, "1"));
        viewItemList.add(new ListViewItem(R.drawable.two, "2"));
        viewItemList.add(new ListViewItem(R.drawable.three, "3"));
        viewItemList.add(new ListViewItem(R.drawable.four, "4"));

        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        Bundle b = new Bundle();
        b.putString("header", "Tram" + ";" + viewItemList.get(viewHolder.getLayoutPosition()).text);
        Intent intent = new Intent(this, TramStatusActivity.class);
        intent.putExtras(b);
        startActivity(intent);

    }

    @Override
    public void onTopEmptyRegionClick() {

    }


}
