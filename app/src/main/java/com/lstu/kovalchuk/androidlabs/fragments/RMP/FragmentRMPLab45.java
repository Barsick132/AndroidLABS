package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lstu.kovalchuk.androidlabs.R;

public class FragmentRMPLab45 extends Fragment {

    private static final String TAG = "FragmentRMPLab45";

    private FragmentRMPLab45_1 fragmentRMPLab45_1;
    private FragmentRMPLab45_2 fragmentRMPLab45_2;
    private FragmentTransaction ftrans;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rmplab45, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentRMPLab45_1 = new FragmentRMPLab45_1();
        fragmentRMPLab45_2 = new FragmentRMPLab45_2();

        ftrans = getActivity().getSupportFragmentManager().beginTransaction();
        ftrans.replace(R.id.mainContainer, fragmentRMPLab45_1);
        ftrans.commit();

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.mainBottomNavView);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            ftrans = getActivity().getSupportFragmentManager().beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_global:
                    ftrans.replace(R.id.mainContainer, fragmentRMPLab45_1);
                    ftrans.commit();
                    return true;
                case R.id.action_history:
                    ftrans.replace(R.id.mainContainer, fragmentRMPLab45_2);
                    ftrans.commit();
                    return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_global);

        SQLiteDatabase db = getActivity().getBaseContext().openOrCreateDatabase("app.db", getContext().MODE_PRIVATE, null);
        db.execSQL("create table if not exists request (" +
                "rqt_id int not null primary key," +
                "rqt_url varchar(2048) not null," +
                "rqt_comment text," +
                "rqt_imgsource varchar(2048) not null," +
                "cli_fullname varchar(500)," +
                "cli_email varchar(500)," +
                "cli_phone varchar(10)," +
                "cli_address varchar(500));");
        db.execSQL("create unique index if not exists request_rqt_id_uindex on request (rqt_id);");
        db.close();


    }
}
