package com.itpteam11.visualisemandai;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This custom RecyclerView adapter will create and hold multiple notification
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private List<NotificationItem> notificationList;
    private Context context;
    HashMap<Integer, String> uris = new HashMap<>();
    HashMap<Integer, String> paths = new HashMap<>();
    private int listSize;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
        this.listSize = notificationList.size();
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;
        context = parent.getContext();

        FirebaseStorage storage = FirebaseStorage.getInstance();

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
        int newListSize = getItemCount();
        int diffInSize = newListSize - listSize;
        if (newListSize != listSize) {
            listSize = newListSize;
            uris.clear();
            HashMap<Integer, String> tempPaths = new HashMap<>();
            for(Map.Entry<Integer, String> pair : paths.entrySet()) {
                Integer pos = pair.getKey();
                String filePath = pair.getValue();
                pos += diffInSize;
                tempPaths.put(pos, filePath);
            }
            paths = tempPaths;
        }

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
                        normalNotificationViewHolder.proxi.setText(proximi + "m away");
                        normalNotificationViewHolder.proxi.setBackgroundColor(Color.RED);
                        normalNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    } else {
                        normalNotificationViewHolder.proxi.setText(proximi + "m away");
                        normalNotificationViewHolder.proxi.setBackgroundColor(Color.GREEN);
                        normalNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    }
                }
                else {
                    normalNotificationViewHolder.proxi.setText("");
                    normalNotificationViewHolder.proxi.setBackgroundColor(Color.WHITE);
                    normalNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                }
                break;
            case 1:
                Log.d(TAG, "In EscapeNotificationViewHolder");
                EscapeNotificationViewHolder escapeNotificationViewHolder = (EscapeNotificationViewHolder) holder;
                escapeNotificationViewHolder.message.setText(notification.getContent());
                escapeNotificationViewHolder.sender.setText(notification.getSender());
                escapeNotificationViewHolder.timestamp.setText(new SimpleDateFormat("dd MMM yyyy h:mm a").format(new Date(notification.getTimestamp())));
                if (notification.getProxi() != null) {
                    double proximi = notification.getProxi();
                    if (proximi > 200.0) {
                        escapeNotificationViewHolder.proxi.setText(proximi + "m away");
                        escapeNotificationViewHolder.proxi.setBackgroundColor(Color.RED);
                        escapeNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    } else {
                        escapeNotificationViewHolder.proxi.setText(proximi + "m away");
                        escapeNotificationViewHolder.proxi.setBackgroundColor(Color.GREEN);
                        escapeNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    }
                }
                else {
                    escapeNotificationViewHolder.proxi.setText("");
                    escapeNotificationViewHolder.proxi.setBackgroundColor(Color.WHITE);
                    escapeNotificationViewHolder.proxi.setTextColor(Color.WHITE);
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
                    double proximi = notification.getProxi();
                    if (proximi > 200.0) {
                        imageNotificationViewHolder.proxi.setText(proximi + "m away");
                        imageNotificationViewHolder.proxi.setBackgroundColor(Color.RED);
                        imageNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    } else {
                        imageNotificationViewHolder.proxi.setText(proximi + "m away");
                        imageNotificationViewHolder.proxi.setBackgroundColor(Color.GREEN);
                        imageNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                    }
                }
                else {
                    imageNotificationViewHolder.proxi.setText("");
                    imageNotificationViewHolder.proxi.setBackgroundColor(Color.WHITE);
                    imageNotificationViewHolder.proxi.setTextColor(Color.WHITE);
                }

                final ImageView imgView = imageNotificationViewHolder.img;
                final String imgName = notification.getImageName();
                final String notiContent = notification.getContent();
                File imagePath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + imgName); // Path to check if it exists in directory
                imgView.setTag(position);

                Log.d(notiContent, "imgView.getDrawable(): " + imgView.getDrawable());
                Log.d(notiContent, "imgView.getTag(): " + imgView.getTag());
                Log.d(notiContent, "imgName: " + imgName);

                StorageReference storRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://visualise-mandai.appspot.com/custom_alerts/");

                Log.d(notiContent, "Current position: " + position);

                List tasks = storRef.getActiveDownloadTasks();

                Log.d(notiContent, "Active tasks: " + tasks.toString());

                // If image doesn't exist in the directory yet
                if (!imagePath.exists()) {

                    // Create image file in directory
                    File imgFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imgName);
                    Log.d(notiContent, "Saved file in dir imgFile: " + imgFile);

                    // If image has previously been downloaded and cached by Picasso
                    if (uris.get(position) != null) {
                        displayImage(Uri.parse(uris.get(position)), imgView);
                    }

                    // If image URL has not been downloaded and displayed by Picasso
                    else {
                        StorageReference imgRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://visualise-mandai.appspot.com/custom_alerts/" + imgName);
                        // Download image to file in directory
                        downloadImage(imgRef, imgFile, position);
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(notiContent, "Got the URL for image, ImageView in focus is in position: " + position);
                                Log.d(notiContent, "URI for the image that has just been downloaded is: " + uri);
                                displayImage(uri, imgView);
                                uris.put(position, uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                }

                // If image exists in directory, set thumbnail of image
                else {
                    Log.d(notiContent, "Image exists in directory, ImageView in focus is in position: " + position);

                    // If image has previously been downloaded and cached by Picasso
                    if (uris.get(position) != null) {
                        Log.d(notiContent, "Image URL has been downloaded - thumnnail exist to display");
                        displayImage(Uri.parse(uris.get(position)), imgView);
                    }

                    // If image URL has not been downloaded and displayed by Picasso
                    else {
                        StorageReference imgRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://visualise-mandai.appspot.com/custom_alerts/" + imgName);
                        Log.d(notiContent, "Image URL has not been downloaded - no thumnnail to display");
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                displayImage(uri, imgView);
                                uris.put(position, uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                }

                break;
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

        public NormalNotificationViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.notification_list_message);
            sender = (TextView) view.findViewById(R.id.notification_list_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_timestamp);
            proxi = (TextView) view.findViewById(R.id.notification_list_proxi);
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

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    String pathToOpen = paths.get(img.getTag());
                    Log.d(TAG, "ONCLICK, Path to open: " + pathToOpen + ", position: " + img.getTag());
                    if (pathToOpen != null){
                        Uri imgUri = Uri.parse("file:" + pathToOpen);
                        intent.setDataAndType(imgUri, "image/*");
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Please wait a moment while the image is getting downloaded...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            message = (TextView) view.findViewById(R.id.notification_list_image_message);
            sender = (TextView) view.findViewById(R.id.notification_list_image_sender);
            timestamp = (TextView) view.findViewById(R.id.notification_list_image_timestamp);
            proxi = (TextView) view.findViewById(R.id.notification_list_image_proxi);
            img = (ImageView) view.findViewById(R.id.notification_list_image_imageview);
        }
    }

    /**
     * Download image from Firebase Storage into file that has been created
     *
     * @param ref
     * @param imgFile
     */
    public void downloadImage(StorageReference ref, final File imgFile, final int pos) {
        paths.put(pos, imgFile.getAbsolutePath());
        ref.getFile(imgFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.d(TAG, "taskSnapshot String: " + taskSnapshot.toString());
                Long bytes = taskSnapshot.getBytesTransferred();
                Log.d(TAG, "taskSnapshot getBytesTransferred: " + bytes);
                Long totalByteCount = taskSnapshot.getTotalByteCount();
                Log.d(TAG, "taskSnapshot gettotalByteCount: " + totalByteCount);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }

        });
    }

    /**
     * Display image by Picasso with uri and ImageView provided
     *
     * @param uri
     * @param view
     */
    public void displayImage(Uri uri, ImageView view) {
        Picasso.with(context)
                .load(uri)
                .placeholder(R.drawable.default_vm_icon)
                .error(R.drawable.img_loading_err)
                .resize(96, 96)
                .centerCrop()
                .into(view);
    }
}