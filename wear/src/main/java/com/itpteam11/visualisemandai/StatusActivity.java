package com.itpteam11.visualisemandai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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

public class StatusActivity extends Activity implements WearableListView.ClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private List<ListViewItem> viewItemList = new ArrayList<>();
    TextView mHeader;
    String header;

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError=false;
    int send = 0;

    public static String TAG = "StatusActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Bundle bundle = this.getIntent().getExtras();
        header = bundle.getString("header");
        mHeader = (TextView)findViewById(R.id.textView);
        mHeader.setText(header);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.wearable_list_view);
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Meal Break"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Toilet Break"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "End Work"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "Back to Work"));


        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        //Toast.makeText(this, "Click on " + viewItemList.get(viewHolder.getLayoutPosition()).text, Toast.LENGTH_SHORT).show();
        String key = viewItemList.get(viewHolder.getLayoutPosition()).text;
        sendMessage(header+"--"+key);
        if(send == 1) {
            Intent intent = new Intent(this, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Success!");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Log.d(TAG, "connected");
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), Key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                            else{
                                send = 1;
                            }
                        }
                    }
            );
        }else{
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();

        }

    }

}
