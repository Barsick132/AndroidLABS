package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lstu.kovalchuk.androidlabs.MainActivity;
import com.lstu.kovalchuk.androidlabs.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static android.Manifest.permission.READ_CONTACTS;

public class FragmentRMPLab3 extends Fragment {

    private static final String TAG = "FragmentRMPLab3";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static boolean mReadContactsPermissionGranted = false;

    public WebView wvBrowser;
    private EditText etUrl;
    private ImageButton btnSearch;
    private String str;
    private StringBuilder contacts = null;
    private Context applicationContext;

    public FragmentRMPLab3() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        applicationContext = activity.getApplicationContextForFragment();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rmplab3, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onStart() {
        super.onStart();

        wvBrowser = getView().findViewById(R.id.rmp_lab3_webView);
        wvBrowser.getSettings().setJavaScriptEnabled(true);
        wvBrowser.loadUrl("https://google.com");
        wvBrowser.setWebViewClient(new MyWebViewClient());

        etUrl = getView().findViewById(R.id.rmp_lab3_etUrl);
        btnSearch = getView().findViewById(R.id.rmp_lab3_btnSearch);
        btnSearch.setOnClickListener(view -> {
            str = etUrl.getText().toString();
            // https://www.google.ru/search?q=asdasd&newwindow=1&lr=lang_ru&sa=X
            try {
                if (str.equals("myHtml")) {
                    wvBrowser.loadUrl("file:///android_asset/index.html");
                } else {
                    URL url = new URL(str);
                    wvBrowser.loadUrl(url.toString());
                }
            } catch (MalformedURLException e) {
                wvBrowser.loadUrl("https://www.google.ru/search?q=" + str +
                        "&newwindow=1&lr=lang_ru&sa=X");
            }
        });

        getReadContactsPermission();
        if (mReadContactsPermissionGranted) getContactList();
    }

    private void getContactList() {
        try (FileInputStream fin = getActivity().openFileInput("Contacts.txt")) {
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String text = new String(bytes);
            contacts = new StringBuilder(text);
        } catch (IOException ex) {
            Log.e(TAG, "getContactList: не удалось открыть файл");

            try (FileOutputStream fos = getActivity().openFileOutput("Contacts.txt", Context.MODE_PRIVATE)) {
                Cursor phones = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                contacts = new StringBuilder("[");
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contacts.append("{name: \"").append(name).append("\", number: \"").append(phoneNumber).append("\"},");

                }
                contacts.substring(contacts.length() - 1);
                contacts.append("]");
                phones.close();

                fos.write(contacts.toString().getBytes());
                Log.d(TAG, "getContactList: контакты успешно записаны в файл");
            } catch (Exception ex1) {
                Log.e(TAG, "getContactList: не удалось записать контакты в файл");
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        // Для старых устройств
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (contacts == null) return;
            if (str != null && str.equals("myHtml"))
                view.loadUrl("javascript:getContacts(" + contacts.toString() + ")");
        }
    }

    // Получение прав доступа к данным
    private void getReadContactsPermission() {
        Log.d(TAG, "getLocationPermission: получение прав доступа к данным местоположения");
        String[] permission = {READ_CONTACTS};

        // Проверка предоставления прав доступа к данным местоположения
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mReadContactsPermissionGranted = true;
        } else {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа к данным местоположения
            requestPermissions(permission, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    // Функция обработки результата получения прав доступа
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // Если хоть какое-то права отсутствует
                            mReadContactsPermissionGranted = false;
                            return;
                        }
                    }
                    // Если все права были предоставлены
                    mReadContactsPermissionGranted = true;
                    getContactList();
                }
            }
        }
    }
}


