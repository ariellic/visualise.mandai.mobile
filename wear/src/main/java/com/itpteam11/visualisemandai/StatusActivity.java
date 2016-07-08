package com.itpteam11.visualisemandai;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;



import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.ImageView;
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

//import static com.itpteam11.visualisemandai.R.drawable.ic_local_dining_black_24dp;

public class StatusActivity extends Activity implements WearableListView.ClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private List<ListViewItem> viewItemList = new ArrayList<>();
    TextView mHeader;
    String header;

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError=false;
    int send = 0;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

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
        
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String previousStatus = prefs.getString("status", null);
        if(previousStatus!= null) {
            if (previousStatus.equals("Toilet Break")) {
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Meal Break"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Back to Work"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "End Work"));

            } else if (previousStatus.equals("Meal Break")) {
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Back to Work"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Toilet Break"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "End Work"));

            }
            else{
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Meal Break"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "Toilet Break"));
                viewItemList.add(new ListViewItem(R.drawable.ic_running, "End Work"));
            }
        }
        else{
            viewItemList.add(new ListViewItem(R.drawable.ic_running, "Meal Break"));
            viewItemList.add(new ListViewItem(R.drawable.ic_running, "Toilet Break"));
            viewItemList.add(new ListViewItem(R.drawable.ic_running, "End Work"));
        }

        /*if(previousStatus!= null) {
            if (previousStatus.equals("Toilet Break")) {
                viewItemList.add(new ListViewItem(R.drawable.meal, "Meal Break"));
                viewItemList.add(new ListViewItem(R.drawable.backtowork, "Back to Work"));
                viewItemList.add(new ListViewItem(R.drawable.endwork, "End Work"));

            } else if (previousStatus.equals("Meal Break")) {
                viewItemList.add(new ListViewItem(R.drawable.backtowork, "Back to Work"));
                viewItemList.add(new ListViewItem(R.drawable.toilet, "Toilet Break"));
                viewItemList.add(new ListViewItem(R.drawable.endwork, "End Work"));

            }
            else{
                viewItemList.add(new ListViewItem(R.drawable.meal, "Meal Break"));
                viewItemList.add(new ListViewItem(R.drawable.toilet, "Toilet Break"));
                viewItemList.add(new ListViewItem(R.drawable.endwork, "End Work"));
            }
        }
        else{
            viewItemList.add(new ListViewItem(R.drawable.meal, "Meal Break"));
            viewItemList.add(new ListViewItem(R.drawable.toilet, "Toilet Break"));
            viewItemList.add(new ListViewItem(R.drawable.endwork, "End Work"));
        }*/

        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        String key = viewItemList.get(viewHolder.getLayoutPosition()).text;
        sendMessage("status;"+key);
        
        if(send == 1) {
            //Store current status in shared preference
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("status", key);
            editor.apply();
            
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
            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        }
    }

    /*
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {
        Toast.makeText(this, "resolve", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(this, "call", Toast.LENGTH_LONG).show();

        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "suspend", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        Log.v(TAG, "onConnectionFailed:" + result.getErrorCode() + "," + result.getErrorMessage());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((StatusActivity) getActivity()).onDialogDismissed();
        }



    }

}
