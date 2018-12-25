package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lstu.kovalchuk.androidlabs.R;

import java.util.List;

public class FragmentRMPLab6 extends Fragment {

    private static final String TAG = "FragmentRMPLab6";

    private LinearLayout llHome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rmplab6, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        llHome = getView().findViewById(R.id.rmp_lab6_HomeLayout);
        llHome.removeAllViews();

        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sen : listSensor) {
            sensorManager.registerListener(new SensorEventListener() {
                private TextView[] arrTV = CreateNewCard(getActivity(), sen.getName());

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    for (int i = 0; i < sensorEvent.values.length; i++) {
                        sb1.append("value[").append(i).append("]\n");
                        sb2.append(String.format("%-8.5f", sensorEvent.values[i])).append("\n");
                        Log.d(TAG, "onSensorChanged: " + sensorEvent.values[i]);
                    }
                    UpdateDataInTable(arrTV, new String[]{sb1.toString(), sb2.toString()});
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    Log.d(TAG, "onAccuracyChanged: sensor: " + sensor + "; value: " + i);
                }
            }, sen, SensorManager.SENSOR_DELAY_NORMAL);
        }
        /*
        sensorManager.registerListener(new SensorEventListener() {
            private TextView[] arrTV = CreateNewCard(getActivity(), gyroscope.getName());

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for (int i = 0; i < sensorEvent.values.length; i++) {
                    sb1.append("value[").append(i).append("]\n");
                    sb2.append(sensorEvent.values[i]).append("\n");
                    Log.d(TAG, "onSensorChanged: " + sensorEvent.values[i]);
                }
                UpdateDataInTable(arrTV, new String[]{sb1.toString(), sb2.toString()});
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Log.d(TAG, "onAccuracyChanged: sensor: " + sensor + "; value: " + i);
            }
        }, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        */
    }

    private void UpdateDataInTable(TextView[] arrTV, String[] data) {
        arrTV[0].setText(data[0]);
        arrTV[1].setText(data[1]);
    }

    private TextView[] CreateNewCard(Context context, String name) {
        final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(24, 12, 24, 12);
        CardView cvCard = new CardView(context);
        llHome.addView(cvCard, layoutParams1);

        final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout llBorder = new LinearLayout(context);
        llBorder.setOrientation(LinearLayout.VERTICAL);
        llBorder.setPadding(10, 10, 10, 10);
        cvCard.addView(llBorder, layoutParams2);

        TextView tvSensorName = new TextView(getActivity());
        tvSensorName.setText(name);
        tvSensorName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        tvSensorName.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvSensorName.setTextSize(20);
        tvSensorName.setTypeface(tvSensorName.getTypeface(), Typeface.BOLD);
        llBorder.addView(tvSensorName, layoutParams2);

        final LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams3.setMargins(0, 10, 0, 0);

        LinearLayout llData = new LinearLayout(context);
        llData.setOrientation(LinearLayout.HORIZONTAL);
        llBorder.addView(llData, layoutParams3);

        final LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams
                (0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        TextView tv1 = new TextView(context);
        tv1.setTextColor(Color.BLACK);
        tv1.setPadding(10, 5, 10, 5);
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        llData.addView(tv1, layoutParams4);

        TextView tv2 = new TextView(context);
        tv2.setTextColor(Color.BLACK);
        tv2.setPadding(10, 5, 10, 5);
        tv2.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        llData.addView(tv2, layoutParams4);

        return new TextView[]{tv1, tv2};
    }
}
