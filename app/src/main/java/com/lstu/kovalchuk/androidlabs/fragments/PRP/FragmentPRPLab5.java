package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lstu.kovalchuk.androidlabs.R;

public class FragmentPRPLab5 extends Fragment {

    private static final String TAG = "FragmentPRPLab5";

    private FragmentPRPLab5_1 fragmentPRPLab5_1;
    private FragmentPRPLab5_2 fragmentPRPLab5_2;
    private FragmentTransaction ftrans;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab5, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        fragmentPRPLab5_1 = new FragmentPRPLab5_1();
        fragmentPRPLab5_2 = new FragmentPRPLab5_2();

        ftrans = getActivity().getSupportFragmentManager().beginTransaction();

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.prp_lab5_NavBottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            ftrans = getActivity().getSupportFragmentManager().beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_subscriber:
                    ftrans.replace(R.id.prp_lab5_Container, fragmentPRPLab5_1);
                    ftrans.commit();
                    return true;
                case R.id.action_publisher:
                    ftrans.replace(R.id.prp_lab5_Container, fragmentPRPLab5_2);
                    ftrans.commit();
                    return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_subscriber);

    }
}
