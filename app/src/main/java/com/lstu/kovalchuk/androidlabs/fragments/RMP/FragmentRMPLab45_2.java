package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lstu.kovalchuk.androidlabs.DataAdapter;
import com.lstu.kovalchuk.androidlabs.R;
import com.lstu.kovalchuk.androidlabs.RequestData;

import java.util.ArrayList;
import java.util.List;

public class FragmentRMPLab45_2 extends Fragment {

    private static final String TAG = "FragmentRMPLab45_2";

    private RecyclerView recyclerView;
    private TextView tvNothingFound;

    private List<RequestData> requestDataList;
    private SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rmplab45_2, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getView().findViewById(R.id.frg2Recycler);
        tvNothingFound = getView().findViewById(R.id.frg2NothingFound);
    }

    @Override
    public void onStart() {
        super.onStart();
        db = getActivity().getBaseContext().openOrCreateDatabase("app.db", Context.MODE_PRIVATE, null);

        requestDataList = new ArrayList<>();
        RequestData requestData;
        Cursor query = db.rawQuery("SELECT * FROM request;", null);
        if (query.moveToFirst()) {
            do {
                requestData = new RequestData();
                requestData.setRqt_id(query.getInt(0));
                requestData.setuRL(query.getString(1));
                requestData.setComment(query.getString(2));
                requestData.setImagesource(query.getString(3));
                requestData.setFullName(query.getString(4));
                requestData.setEmail(query.getString(5));
                requestData.setPhone(query.getString(6));
                requestData.setAddress(query.getString(7));
                requestDataList.add(requestData);
            }
            while (query.moveToNext());
        }
        query.close();

        if(requestDataList.size()==0){
            tvNothingFound.setVisibility(View.VISIBLE);
            return;
        }else {
            tvNothingFound.setVisibility(View.GONE);
        }

        DataAdapter dataAdapter = new DataAdapter(getActivity(), requestDataList);
        recyclerView.setAdapter(dataAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }
}

