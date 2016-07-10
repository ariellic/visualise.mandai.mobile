package com.itpteam11.visualisemandai;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 *
 */
public class SendNotificationFragment extends Fragment {
    Button msgButton;

    public SendNotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send_notification, container, false);
        msgButton = (Button) view.findViewById(R.id.message);
        msgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CustomMessageActivity.class);
                startActivity(intent);
                /*
                FragmentManager fragManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragTrans = fragManager.beginTransaction();
                CustomMessageFragment cusMsgFrag = new CustomMessageFragment();
                ViewGroup container = (ViewGroup)getView();
                container.removeView(getActivity().findViewById(R.id.));
                fragTrans.replace(((ViewGroup) getView()).getId(), cusMsgFrag);
                fragTrans.addToBackStack(null);
                fragTrans.commit();
                */
            }
        });
        return view;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
