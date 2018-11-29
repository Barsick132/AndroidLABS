package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.lstu.kovalchuk.androidlabs.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class FragmentRMPLab2 extends Fragment {

    private static final String TAG = "FragmentRMPLab2";
    private final int REQUEST_CODE_PHOTO = 1;

    private MaterialEditText metMessage;
    private ImageView ivPhoto;
    private Uri uri;
    private File file;

    public FragmentRMPLab2() {
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
        return inflater.inflate(R.layout.fragment_rmplab2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        metMessage = getView().findViewById(R.id.rmp_lab2_Message);
        Button btnSend = getView().findViewById(R.id.rmp_lab2_Send);
        btnSend.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, metMessage.getText().toString()); // Вместо My message упаковываете текст, который необходимо передать
            shareIntent.setType("text/plane");
            startActivity(Intent.createChooser(shareIntent, "Поделиться в")); // Share text - заголовок диалога выбора необходимого приложения
        });
        ivPhoto = getView().findViewById(R.id.rmp_lab2_Photo);
        Button btnCreatePhoto = getView().findViewById(R.id.rmp_lab2_CreatePhoto);
        btnCreatePhoto.setOnClickListener(view -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                ivPhoto.setImageBitmap((Bitmap) intent.getExtras().get("data"));
                ivPhoto.setPadding(1,1,1,1);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
