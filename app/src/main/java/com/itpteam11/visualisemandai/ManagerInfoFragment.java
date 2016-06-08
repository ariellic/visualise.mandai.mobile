package com.itpteam11.visualisemandai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class ManagerInfoFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomCardAdapter customCardAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    private HashMap<Integer, String> cardDataSet;

    public ManagerInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardDataSet = new HashMap<Integer, String>();
        cardDataSet.put(CardType.STAFF_AVAILABLE, "12");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manager_info, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.manager_info_fragment_recyclerView);
        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        customCardAdapter = new CustomCardAdapter(cardDataSet);
        recyclerView.setAdapter(customCardAdapter);

        return view;
    }
}
