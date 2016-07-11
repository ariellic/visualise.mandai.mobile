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
 *  a notification will be sent to non off duty relevant staff notifying them to send more trams
 *  to the station.
 */

public class TramActivity extends Activity implements WearableListView.ClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Bundle bundle = this.getIntent().getExtras();
        header = bundle.getString("tramheader");
        mHeader = (TextView)findViewById(R.id.textView);
        mHeader.setText(header);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.image_list_view);

        /*viewItemList.add(new ListViewItem(R.drawable.ic_running, "1"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "2"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "3"));
        viewItemList.add(new ListViewItem(R.drawable.ic_running, "4"));*/

        viewItemList.add(new ListViewItem(R.drawable.one, "1"));
        viewItemList.add(new ListViewItem(R.drawable.two, "2"));
        viewItemList.add(new ListViewItem(R.drawable.three, "3"));
        viewItemList.add(new ListViewItem(R.drawable.four, "4"));

        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        //  Toast.makeText(this, "Click on " + viewItemList.get(viewHolder.getLayoutPosition()).text, Toast.LENGTH_SHORT).show();
        String key = viewItemList.get(viewHolder.getLayoutPosition()).text;
        sendMessage("tram;" + key);
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
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed:" + connectionResult.getErrorCode() + ", " + connectionResult.getErrorMessage());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
            Log.d(TAG, "Watch connected");
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
                                Intent intent = new Intent(getBaseContext(), ConfirmationActivity.class);
                                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                        ConfirmationActivity.SUCCESS_ANIMATION);
                                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Notice Sent!");
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
            );
        } else{
            Toast.makeText(this, "Watch not connected", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
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

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog( this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((TramActivity) getActivity()).onDialogDismissed();
        }
    }

}
