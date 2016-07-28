package com.itpteam11.visualisemandai;

import android.view.View;
import android.widget.TextView;
import android.widget.CheckBox;

/**
 * This object contains the textView that will contain the name of a staff and a checkbox to be used for
 * sending custom alerts to recipients
 */
public class RecipientsViewHolder {

    private TextView textView;
    private CheckBox checkBox;
    public View view;

    public RecipientsViewHolder(View view) {
        this.view = view;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }
}
