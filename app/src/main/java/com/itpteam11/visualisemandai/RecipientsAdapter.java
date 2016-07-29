package com.itpteam11.visualisemandai;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter is responsible for holding the recipients in the recipients list
 */
public class RecipientsAdapter extends BaseAdapter {

    List<User> recipientsList;
    Activity context;
    boolean[] itemChecked;
    ArrayList<String> checkedValue;

    public RecipientsAdapter(Activity context, List <User> recipientsList) {
        super();
        this.context = context;
        this.recipientsList = recipientsList;
        itemChecked = new boolean[recipientsList.size()];
        checkedValue = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return recipientsList.size();
    }

    @Override
    public Object getItem(int position) {
        return recipientsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final RecipientsViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_recipient, null);
            holder = new RecipientsViewHolder(convertView);

            holder.setTextView((TextView) convertView
                    .findViewById(R.id.userNameStatus));
            holder.setCheckBox((CheckBox) convertView
                    .findViewById(R.id.checkBox));

            convertView.setTag(holder);

        } else {
            holder = (RecipientsViewHolder) convertView.getTag();
        }

        // Set default to all named not checked
        holder.getTextView().setText(recipientsList.get(position).getName());
        holder.getCheckBox().setChecked(false);

        // Check the checkbox if it has been checked
        if (itemChecked[position]){
            holder.getCheckBox().setChecked(true);
        } else {
            holder.getCheckBox().setChecked(false);
        }

        // If checkbox is clicked, add or remove the name from the checked users list
        holder.getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textViewValue = holder.getTextView().getText().toString();
                // If checkbox has been checked & it has not been added into the list of checked
                // users yet
                if (holder.getCheckBox().isChecked() && !checkedValue.contains(textViewValue)){
                    itemChecked[position] = true;
                    checkedValue.add(textViewValue);
                }
                // If the checbox has been unchecked and it has been added into the list of checked users
                else if (!holder.getCheckBox().isChecked() && checkedValue.contains(textViewValue)) {
                    itemChecked[position] = false;
                    checkedValue.remove(textViewValue);
                }
            }
        });

        return convertView;
    }
}