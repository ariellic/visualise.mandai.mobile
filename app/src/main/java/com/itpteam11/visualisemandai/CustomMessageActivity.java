package com.itpteam11.visualisemandai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomMessageActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "CustomMessageActivity";
    private String mCurrentPhotoPath;
    private Bitmap mImageBitmap;

    private EditText editTextMessage;
    private Button nextButton;
    private Button camButton;
    private ImageView imageView;
    private TextView textViewImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_message);

        //Setup Action bar
        getSupportActionBar().setTitle("Send custom message");

        editTextMessage = (EditText) findViewById(R.id.edit_text_message);
        nextButton = (Button) findViewById(R.id.button_next);
        camButton = (Button) findViewById(R.id.button_camera);
        imageView = (ImageView) findViewById(R.id.imageview_picture);
        textViewImages = (TextView) findViewById(R.id.textview_images);
        mCurrentPhotoPath = "";

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(v.getContext(), CustomMessageRecipientsActivity.class);
                String message = editTextMessage.getText().toString();
                intent.putExtra("CustomMessage", message);
                intent.putExtra("ImagePath", mCurrentPhotoPath);
                startActivity(intent);
            }
        });

        camButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "resolveActivity(getPackageManager()) not null");
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i(TAG, "IOException - unable to create image file");
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Log.d(TAG, "Image file not null");
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile)); // Photo captured will be saved
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    } else {
                        Log.d(TAG, "Image file null");
                    }
                }
                
            }
        });;


        /**
         * Allow the image to be opened in image viewer to preview before sending
         */
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri imgUri = Uri.parse("file:" + mCurrentPhotoPath);
                intent.setDataAndType(imgUri, "image/*");
                startActivity(intent);
                return true;
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // To display a thumbnail of image captured in the activity
            mImageBitmap = getThumbnail(mCurrentPhotoPath);
            imageView.setImageBitmap(mImageBitmap);
            textViewImages.setText("Added image:");
        }
    }

    /**
     * To create a image file with a unique name to save the photo that will be captured via the camera
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // Make sure that the file's name is unique
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); ///storage/emulated/0/Android/data/com.itpteam11.visualisemandai/files/Pictures
        File image = new File(storageDir, imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * To get the thumbnail of the photo that has been captured
     * @param path
     * @return
     */
    public static Bitmap getThumbnail(String path)
    {
        Bitmap imgThumbBitmap = null;
        try
        {
            final int THUMBNAIL_SIZE = 128;

            FileInputStream in = new FileInputStream(path);
            imgThumbBitmap = BitmapFactory.decodeStream(in);

            imgThumbBitmap = Bitmap.createScaledBitmap(imgThumbBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            imgThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch(Exception ex) {
        }
        return imgThumbBitmap;
    }

}
