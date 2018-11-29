package com.lstu.kovalchuk.androidlabs.fragments.RMP;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import com.lstu.kovalchuk.androidlabs.R;

public class FragmentRMPLab1 extends Fragment {

    private Button btnSum;
    private Button btnSLeft;
    private Button btnSRight;
    private Button btnInvers;
    private Button btnXOR;
    private ToggleButton tb11;
    private ToggleButton tb12;
    private ToggleButton tb13;
    private ToggleButton tb14;
    private ToggleButton tb21;
    private ToggleButton tb22;
    private ToggleButton tb23;
    private ToggleButton tb24;
    private ToggleButton tb31;
    private ToggleButton tb32;
    private ToggleButton tb33;
    private ToggleButton tb34;
    private ToggleButton tb35;

    public FragmentRMPLab1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_rmplab1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        tb11 = getView().findViewById(R.id.rmp_lab1_tb11);
        tb12 = getView().findViewById(R.id.rmp_lab1_tb12);
        tb13 = getView().findViewById(R.id.rmp_lab1_tb13);
        tb14 = getView().findViewById(R.id.rmp_lab1_tb14);
        tb21 = getView().findViewById(R.id.rmp_lab1_tb21);
        tb22 = getView().findViewById(R.id.rmp_lab1_tb22);
        tb23 = getView().findViewById(R.id.rmp_lab1_tb23);
        tb24 = getView().findViewById(R.id.rmp_lab1_tb24);
        tb31 = getView().findViewById(R.id.rmp_lab1_tb31);
        tb32 = getView().findViewById(R.id.rmp_lab1_tb32);
        tb33 = getView().findViewById(R.id.rmp_lab1_tb33);
        tb34 = getView().findViewById(R.id.rmp_lab1_tb34);
        tb35 = getView().findViewById(R.id.rmp_lab1_tb35);

        btnSum = getView().findViewById(R.id.rmp_lab1_buttonSum);
        btnSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] arrayNumbers = getNumbers();
                int result = Integer.parseInt(arrayNumbers[0], 2) +
                        Integer.parseInt(arrayNumbers[1], 2);
                printResult(Integer.toBinaryString(result));
            }
        });
        btnSLeft = getView().findViewById(R.id.rmp_lab1_buttonSLeft);
        btnSLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String first = getNumbers()[0];
                first = String.format("%s%s%s%s", first.charAt(1), first.charAt(2),
                        first.charAt(3), first.charAt(0));
                printResult(first);
            }
        });
        btnSRight = getView().findViewById(R.id.rmp_lab1_buttonSRight);
        btnSRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String first = getNumbers()[0];
                first = String.format("%s%s%s%s", first.charAt(3), first.charAt(0),
                        first.charAt(1), first.charAt(2));
                printResult(first);
            }
        });
        btnInvers = getView().findViewById(R.id.rmp_lab1_buttonInvers);
        btnInvers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = ~Integer.parseInt(getNumbers()[0], 2);
                String strResult = Integer.toBinaryString(result);
                printResult(strResult.substring(strResult.length()-4));
            }
        });
        btnXOR = getView().findViewById(R.id.rmp_lab1_buttonXOR);
        btnXOR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] arrayNumbers = getNumbers();
                int result = Integer.parseInt(arrayNumbers[0], 2) ^
                        Integer.parseInt(arrayNumbers[1], 2);
                printResult(Integer.toBinaryString(result));
            }
        });
    }

    private String[] getNumbers() {
        String[] arrayNumbers = new String[2];
        arrayNumbers[0] = tb11.getText().toString() + tb12.getText().toString() +
                tb13.getText().toString() + tb14.getText().toString();
        arrayNumbers[1] = tb21.getText().toString() + tb22.getText().toString() +
                tb23.getText().toString() + tb24.getText().toString();
        return arrayNumbers;
    }

    private void printResult(String res) {
        if (res.length() == 1) {
            tb31.setVisibility(View.GONE);
            tb32.setText("0");
            tb33.setText("0");
            tb34.setText("0");
            tb35.setText(String.format("%s", res.charAt(0)));
        }
        if (res.length() == 2) {
            tb31.setVisibility(View.GONE);
            tb32.setText("0");
            tb33.setText("0");
            tb34.setText(String.format("%s", res.charAt(0)));
            tb35.setText(String.format("%s", res.charAt(1)));
        }
        if (res.length() == 3) {
            tb31.setVisibility(View.GONE);
            tb32.setText("0");
            tb33.setText(String.format("%s", res.charAt(0)));
            tb34.setText(String.format("%s", res.charAt(1)));
            tb35.setText(String.format("%s", res.charAt(2)));
        }
        if (res.length() == 4) {
            tb31.setVisibility(View.GONE);
            tb32.setText(String.format("%s", res.charAt(0)));
            tb33.setText(String.format("%s", res.charAt(1)));
            tb34.setText(String.format("%s", res.charAt(2)));
            tb35.setText(String.format("%s", res.charAt(3)));
        }
        if (res.length() == 5) {
            tb31.setVisibility(View.VISIBLE);
            tb31.setText(String.format("%s", res.charAt(0)));
            tb32.setText(String.format("%s", res.charAt(1)));
            tb33.setText(String.format("%s", res.charAt(2)));
            tb34.setText(String.format("%s", res.charAt(3)));
            tb35.setText(String.format("%s", res.charAt(4)));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
