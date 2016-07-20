package com.itpteam11.visualisemandai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This custom RecyclerView adapter will create and hold multiple notification
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> implements DownloadTaskInterface {
    private static final String TAG = "NotificationAdapter";
    private List<NotificationItem> notificationList;
    private StorageReference storageRef;
    StorageReference imgRef;
    private Context context;
    private String mCurrentPhotoPath;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;
        context = parent.getContext();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://visualise-mandai.appspot.com");
        imgRef = null;

        //Create respective notification item based on notification type
        switch (viewType) {
            case 1:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_escape, parent, false);
                return new EscapeNotificationViewHolder(item);
            case 2:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_image, parent, false);
                return new ImageNotificationViewHolder(item);
            default:
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list, parent, false);
                return new NormalNotificationViewHolder(item);
        }
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, final int position) {
        NotificationItem notification = notificationList.get(position);

        //Set respective notification item's widgets with content
        switch (holder.getItemViewType()) {
            case 0:
                Log.d(TAG, "In NormalNotificationViewHolder");
                Log.d(TAG, "Message: " + notification.getContent());
                final NormalNotificationViewHolder normalNotificationViewHolder = (NormalNotificationViewHolder) holder;
                normalNotificationViewHolder.message.setText(notification.getContent());
                normalNotificationViewHolder.sender.setText(notification.getSender());
                normalNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));

                if (notification.getProxi() != null) {
                    double proximi = notification.getProxi();
                    if (proximi > 200.0) {
                        normalNotificationViewHolder.proxi.setText(notification.getProxi() + "m away");
                        normalNotificationViewHolder.proxi.setTextColor(Color.RED);
                    } else {
                        normalNotificationViewHolder.proxi.setText(notification.getProxi() + "m away");
                        normalNotificationViewHolder.proxi.setTextColor(Color.GREEN);
                    }
                }
                break;
            case 1:
                Log.d(TAG, "In EscapeNotificationViewHolder");
                EscapeNotificationViewHolder escapeNotificationViewHolder = (EscapeNotificationViewHolder) holder;
                escapeNotificationViewHolder.message.setText(notification.getContent());
                escapeNotificationViewHolder.sender.setText(notification.getSender());
                escapeNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));
                if (notification.getProxi() != null) {
                    double Eproximi = notification.getProxi();
                    if (Eproximi > 200.0) {
                        escapeNotificationViewHolder.proxi.setText(notification.getProxi() + "m away");
                        escapeNotificationViewHolder.proxi.setTextColor(Color.RED);
                    } else {
                        escapeNotificationViewHolder.proxi.setText(notification.getProxi() + "m away");
                        escapeNotificationViewHolder.proxi.setTextColor(Color.GREEN);
                    }
                }
                escapeNotificationViewHolder.setResolveButtonListener(notification.getNotificationID(), notification.getContent());
                break;
            case 2:
                Log.d(TAG, "In ImageNotificationViewHolder");
                Log.d(TAG, "Message: " + notification.getContent());
                final ImageNotificationViewHolder imageNotificationViewHolder = (ImageNotificationViewHolder) holder;
                imageNotificationViewHolder.message.setText(notification.getContent());
                imageNotificationViewHolder.sender.setText(notification.getSender());
                imageNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));

                if (notification.getProxi() != null) {
                    double Eproximi = notification.getProxi();
                    if (Eproximi > 200.0) {
                        imageNotificationViewHolder.proxi.setText(Eproximi + "m away");
                        imageNotificationViewHolder.proxi.setTextColor(Color.RED);
                    } else {
                        imageNotificationViewHolder.proxi.setText(Eproximi + "m away");
                        imageNotificationViewHolder.proxi.setTextColor(Color.GREEN);
                    }
                }

                ImageView imgView = imageNotificationViewHolder.img;
                Log.d(TAG, "imgView.getDrawable(): " + imgView.getDrawable());
                Log.d(TAG, "imgView.getTag(): " + imgView.getTag());
                if (imgView.getDrawable() != null && imgView.getTag() == null) {
                    imgView.setImageResource(0);
                }

                // Item does not hold an image
                if (imgView.getDrawable() == null) {
                    Log.d(TAG, "No drawable in imageview");
                    // If there is an image for the notification
                    if (!notification.getImageName().equals("NA") && notification.getImageName() != null) {

                        Log.d(TAG, "notification.getImageName() not NA not null");

                        String imgName = notification.getImageName();
                        imgRef = storageRef.child("custom_alerts/" + notification.getImageName());

                        // Create image path
                        File imagePath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + imgName);

                        Log.d(TAG, "imgFile: " + imagePath.toString() + ", " + imagePath.getAbsolutePath());

                        // If image path does not exist yet, create file and download image to file
                        if (!imagePath.exists()) {
                            DownloadImageAsyncTask dlImgTask = new DownloadImageAsyncTask(imgView, imgName, position, this);
                            dlImgTask.execute();

                            //downloadImage(imgRef, imageNotificationViewHolder, position, imgName);
                        }
                        // If image exists in directory, set thumbnail of image
                        else {
                            Log.d(TAG, "Image exist in directory, position: " + position);
                            imgView.setImageBitmap(getThumbnail(imagePath.getAbsolutePath()));
                            imgView.setTag(position);
                            imgView.setVisibility(View.VISIBLE);
                            //this.notifyItemChanged(position);
                        }
                        //String result = imgRef.getFile(imgFile).getResult().toString();
                        //Log.d(TAG, "Result of download: " + result);

                    }
                    // No image available for notification, disable imageview
                    else {
                        if (notification.getImageName().equals("NA")) {
                            Log.d(TAG, "notification.getImageName() is NA");
                        } else if (notification.getImageName() == null) {
                            Log.d(TAG, "notification.getImageName() is null");
                        } else {
                            Log.d(TAG, "notification.getImageName() is " + notification.getImageName());
                        }
                        //Log.d(TAG, "Image exist");
                        imgView.setVisibility(View.GONE);
                    }
                }
                // Item holds an image
                else {
                    Log.d(TAG, "There is a drawable in imageview");
                    // Item's notification should not have an image
                    if (notification.getImageName().equals("NA") || notification.getImageName() == null) {
                        imgView.setImageDrawable(null);
                        imgView.setVisibility(View.GONE);
                    }
                    // Item's notification image is incorrect
                    else if (!imgView.getTag().equals(position)) {
                        imgView.setImageDrawable(null);
                        imgView.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    @Override
    public void asyncComplete(boolean success, int pos) {
        this.notifyItemChanged(pos);
    }

    private class DownloadImageAsyncTask extends AsyncTask<Void, Void, String> {

        String imgName;
        ImageView imgV;
        int position;
        private DownloadTaskInterface delegate;

        public DownloadImageAsyncTask (ImageView view, String name, int pos, DownloadTaskInterface delegate) {
            this.imgV = view;
            this.imgName = name;
            this.position = pos;
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Download task in background");
            //position = Integer.valueOf(params[2]);
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://visualise-mandai.appspot.com/custom_alerts/" + imgName);
            //Log.d(TAG, "imgName: " + imgName + ", position: " + position);
            File imgFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imgName);
            mCurrentPhotoPath = imgFile.getAbsolutePath();
            Log.d(TAG, "saved file imgFile: " + imgFile);
            ref.getFile(imgFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Log.d(TAG, "taskSnapshot String: " + taskSnapshot.toString());
                    Long bytes = taskSnapshot.getBytesTransferred();
                    Log.d(TAG, "taskSnapshot getBytesTransferred: " + bytes);
                    Long totalByteCount = taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "taskSnapshot gettotalByteCount: " + totalByteCount);
                    Log.d(TAG, "mCurrentPhotoPath: " + mCurrentPhotoPath);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            }).add;
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            imgV.setImageBitmap(getThumbnail(mCurrentPhotoPath));
            imgV.setTag(position);
            imgV.setVisibility(View.VISIBLE);
            delegate.asyncComplete(true, position);
        }

    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    //Method for onCreateViewHolder to identify viewType parameter
    @Override
    public int getItemViewType(int position) {
        String type = notificationList.get(position).getType();

        //Set type is normal when no type stated
        if (notificationList.get(position).getType() == null) {
            type = Notification.NORMAL_NOTIFICATION;
        }

        //Return numeric type
        switch (type) {
            case Notification.ESCAPE_NOTIFICATION:
                return 1;
            case Notification.IMAGE_NOTIFICATION:
                return 2;
            default:
                return 0;
        }
    }

    //Super CardView
    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        public NotificationViewHolder(View v) {
            super(v);
        }
    }

    //Normal notification item
    private class NormalNotificationViewHolder extends NotificationViewHolder {
        public TextView message, sender, timestamp, proxi;
        //public ImageView img;

        public NormalNotificationViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_message);
            sender = (TextView) view.findViewById(R.id.notification_list_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_timestamp);
            proxi = (TextView) view.findViewById(R.id.notification_list_proxi);
            //img = (ImageView) view.findViewById(R.id.notification_list_image);
        }
    }

    //Animal escape notification item
    private class EscapeNotificationViewHolder extends NotificationViewHolder {
        private TextView message, sender, timestamp, proxi;
        private Button resolve;

        public EscapeNotificationViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_escape_message);
            sender = (TextView) view.findViewById(R.id.notification_list_escape_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_escape_timestamp);
            resolve = (Button) view.findViewById(R.id.notification_list_escape_resolve_button);
            proxi = (TextView) view.findViewById(R.id.notification_list_escape_proxi);
        }

        //Set listener for Resolve button
        public void setResolveButtonListener(final String notificationID, final String content) {
            resolve.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Set notification type as normal and notice has been resolved
                    FirebaseDatabase.getInstance().getReference().child("notification").child(notificationID).child("type").setValue(Notification.NORMAL_NOTIFICATION);

                    //Get the list of current staff who is not on off
                    FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Boolean> receiver = new HashMap<String, Boolean>();

                            //Populate the list with staff who is not on off
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                if (!child.child("status").getValue(String.class).equals("off")) {
                                    receiver.put(child.getKey(), false);
                                }
                            }

                            //Get user's coordinates to indicate where the animal has captured
                            Double latitude = null;
                            Double longitude = null;
                            if (StaffLocationService.isLocationPermissionGranted()) {
                                latitude = StaffLocationService.getLatitude();
                                longitude = StaffLocationService.getLongitude();
                            }

                            //Create a notification with necessary information to notify staff who is not on off
                            Notification notification = new Notification();
                            notification.sendNotification(Notification.NORMAL_NOTIFICATION, content.split(" has")[0] + " has been captured!", latitude, longitude, FirebaseAuth.getInstance().getCurrentUser().getUid(), receiver, "NA");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to get working staff list
                            System.out.println("Failed to get working staff list: " + error.toException());
                        }
                    });
                }
            });
        }
    }

    //Image notification item
    private class ImageNotificationViewHolder extends NotificationViewHolder {
        public TextView message, sender, timestamp, proxi;
        public ImageView img;

        public ImageNotificationViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_image_message);
            sender = (TextView) view.findViewById(R.id.notification_list_image_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_image_timestamp);
            proxi = (TextView) view.findViewById(R.id.notification_list_image_proxi);
            img = (ImageView) view.findViewById(R.id.notification_list_image_imageview);
        }
    }

    public static Bitmap getThumbnail(String path) {
        Bitmap imgThumbBitmap = null;
        try {
            final int THUMBNAIL_SIZE = 128;

            FileInputStream in = new FileInputStream(path);
            imgThumbBitmap = BitmapFactory.decodeStream(in);

            imgThumbBitmap = Bitmap.createScaledBitmap(imgThumbBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            imgThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception ex) {
        }
        return imgThumbBitmap;
    }
}