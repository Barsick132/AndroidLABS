package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lstu.kovalchuk.androidlabs.R;

public class FragmentPRPLab3 extends Fragment {

    private static final String TAG =  "FragmentPRPLab3";

    private FragmentPRPLab3_1 fragmentPRPLab3_1;
    private FragmentPRPLab3_2 fragmentPRPLab3_2;
    private FragmentTransaction ftrans;

    public FragmentPRPLab3() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentPRPLab3_1 = new FragmentPRPLab3_1();
        fragmentPRPLab3_2 = new FragmentPRPLab3_2();

        ftrans = getActivity().getSupportFragmentManager().beginTransaction();

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.prp_lab3_NavBottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            ftrans = getActivity().getSupportFragmentManager().beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_subscriber:
                    ftrans.replace(R.id.prp_lab3_Container, fragmentPRPLab3_1);
                    ftrans.commit();
                    return true;
                case R.id.action_publisher:
                    ftrans.replace(R.id.prp_lab3_Container, fragmentPRPLab3_2);
                    ftrans.commit();
                    return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_subscriber);
    }
}
