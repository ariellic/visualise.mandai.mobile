package com.itpteam11.visualisemandai.listview;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.itpteam11.visualisemandai.R;

public class ListViewImageRowView extends FrameLayout implements WearableListView.OnCenterProximityListener {

    private ImageView image;

    public ListViewImageRowView(Context context) {
        super(context);
        View.inflate(context, R.layout.image_listview, this);
        image = (ImageView) findViewById(R.id.image);
    }

    @Override
    public void onCenterPosition(boolean b) {
        image.animate().scaleX(1.2f).scaleY(1.2f).alpha(1).setDuration(200);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        image.animate().scaleX(1f).scaleY(1f).alpha(0.6f).setDuration(200);
    }

    public ImageView getImage() {
        return image;
    }
}
