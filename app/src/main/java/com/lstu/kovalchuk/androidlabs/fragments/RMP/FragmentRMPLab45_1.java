package com.lstu.kovalchuk.androidlabs.fragments.RMP;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lstu.kovalchuk.androidlabs.RequestData;
import com.lstu.kovalchuk.androidlabs.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.MaterialMultiAutoCompleteTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class FragmentRMPLab45_1 extends Fragment {

    private static final String TAG = "FragmentRMPLab45_1";

    private CheckBox cbContactData;
    private CardView cvContactData;
    private LinearLayout llProgressBar;

    private MaterialEditText metURL;
    private MaterialMultiAutoCompleteTextView mctComment;
    private MaterialEditText metFullName;
    private MaterialEditText metEmail;
    private MaterialEditText metPhone;
    private MaterialEditText metAddress;
    private SQLiteDatabase db;

    private RequestData requestData;

    enum Method {
        getScreen, getClient, setRequest
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_rmplab45_1, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        llProgressBar = getView().findViewById(R.id.frg1ProgressBar);

        cvContactData = getView().findViewById(R.id.frg1ContactData);
        cbContactData = getView().findViewById(R.id.frg1CheckContactData);
        cbContactData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) cvContactData.setVisibility(View.VISIBLE);
                else cvContactData.setVisibility(View.GONE);
            }
        });

        metURL = getView().findViewById(R.id.frg1URL);
        mctComment = getView().findViewById(R.id.frg1Comment);
        metFullName = getView().findViewById(R.id.frg1FullName);
        metEmail = getView().findViewById(R.id.frg1Email);
        metPhone = getView().findViewById(R.id.frg1Phone);
        metAddress = getView().findViewById(R.id.frg1Address);

        cvContactData = getView().findViewById(R.id.frg1ContactData);
        cbContactData = getView().findViewById(R.id.frg1CheckContactData);
        cbContactData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) cvContactData.setVisibility(View.VISIBLE);
                else cvContactData.setVisibility(View.GONE);
            }
        });

        requestData = new RequestData();

        Button btnSendData = getView().findViewById(R.id.frg1SendRequest);
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //metURL.setError(null);
                if (metURL.getText().length() != 0) {
                    requestData.setuRL(metFilter(metURL));
                    requestData.setComment(metFilter(mctComment));
                    if (cbContactData.isChecked()) {
                        requestData.setFullName(metFilter(metFullName));
                        requestData.setEmail(metFilter(metEmail));
                        if (!metPhone.getText().toString().isEmpty() &&
                                metPhone.getText().toString().length() != 10) {
                            metPhone.setError("Номер должен состоять из 10 цифр");
                            return;
                        }
                        requestData.setPhone(metFilter(metPhone));
                        requestData.setAddress(metFilter(metAddress));
                    }
                } else {
                    metURL.setError("Данное поле обязательно для заполнения");
                    return;
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        showPB(true);
                        new SendRequest();
                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        db = getActivity().getBaseContext().openOrCreateDatabase("app.db", Context.MODE_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    private void showPB(Boolean show) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable runnable;
        if (show) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    llProgressBar.setVisibility(View.VISIBLE);
                }
            };
        } else {
            runnable = new Runnable() {
                @Override
                public void run() {
                    llProgressBar.setVisibility(View.GONE);
                }
            };
        }
        mainHandler.post(runnable);
    }

    private class SendRequest {

        private RequestQueue queue;
        private JsonObjectRequest jsObjRequest;
        private Uri.Builder builder;
        private JSONObject jsonObject;

        SendRequest() {
            getScreen();
        }

        private void start() {
            queue.add(jsObjRequest);
            queue.start();
        }

        private void getScreen() {
            queue = Volley.newRequestQueue(getActivity());

            String ENDPOINT = "https://voice-gen-220900.appspot.com/getScreen/";
            builder = Uri.parse(ENDPOINT).buildUpon();
            builder.appendQueryParameter("URL", requestData.getuRL());

            try {
                jsonObject = new JSONObject("{}");
            } catch (JSONException e) {
                jsonObject = null;
            }

            jsObjRequest = getJsonObjRequest(builder.toString(), jsonObject, Method.getScreen);

            start();
        }

        private void getClient() {
            queue = Volley.newRequestQueue(getActivity());

            String ENDPOINT = "https://voice-gen-220900.appspot.com/getClient/";
            builder = Uri.parse(ENDPOINT).buildUpon();

            Map<String, String> map = new LinkedHashMap<>();
            mapPutFilter(map, "cli_fullname", requestData.getFullName());
            mapPutFilter(map, "cli_email", requestData.getEmail());
            mapPutFilter(map, "cli_phone", requestData.getPhone());
            mapPutFilter(map, "cli_address", requestData.getAddress());

            jsonObject = new JSONObject(map);

            jsObjRequest = getJsonObjRequest(builder.toString(), jsonObject, Method.getClient);

            start();
        }

        private void setRequest() throws JSONException {
            queue = Volley.newRequestQueue(getActivity());

            String ENDPOINT = "https://voice-gen-220900.appspot.com/setRequest/";
            builder = Uri.parse(ENDPOINT).buildUpon();

            StringBuilder sb = new StringBuilder("{");
            sbPutFilter(sb, "cli_id", requestData.getCli_id());
            sbPutFilter(sb, "rqt_url", requestData.getuRL());
            sbPutFilter(sb, "rqt_comment", requestData.getComment());
            sbPutFilter(sb, "rqt_imgsource", requestData.getImagesource());
            sbPutFilter(sb, "dlg_create", false);
            sb.append("}").append("");
            sb.deleteCharAt(sb.length() - 2);

            jsonObject = new JSONObject(sb.toString());

            jsObjRequest = getJsonObjRequest(builder.toString(), jsonObject, Method.setRequest);

            start();
        }

        private JsonObjectRequest getJsonObjRequest(String url, JSONObject jsonObject, Method method) {
            return new JsonObjectRequest(Request.Method.POST, url, jsonObject, getResponseListener(method), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse: ", error);
                    showPB(false);
                    Toast.makeText(getActivity(), "Произошла ошибка при попытке отпраки запроса", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private Response.Listener<JSONObject> getResponseListener(Method method) {
            switch (method) {
                case getScreen:
                    return new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: getScreen good");
                            try {
                                if (response.getString("status").equals("OK")) {
                                    requestData.setImagesource(response.getString("imgsource"));
                                    getClient();
                                } else {
                                    showPB(false);
                                    Toast.makeText(getActivity(), "Сервер почему-то не смог создать скриншот", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "onErrorResponse: ", e);
                                showPB(false);
                                Toast.makeText(getActivity(), "Произошла ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                case getClient:
                    return new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: getClient good");
                            try {
                                if (response.getString("status").equals("OK")) {
                                    requestData.setCli_id(response.getInt("cli_id"));
                                    setRequest();
                                } else {
                                    showPB(false);
                                    Toast.makeText(getActivity(), "Сервер почему-то не смог создать клиента", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "onErrorResponse: ", e);
                                showPB(false);
                                Toast.makeText(getActivity(), "Произошла ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                case setRequest:
                    return new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: setRequest good");
                            try {
                                if (response.getString("status").equals("OK")) {
                                    requestData.setRqt_id(response.getInt("rqt_id"));

                                    showPB(false);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Оповещение")
                                            .setMessage("Заявка успешно отправлена!")
                                            .setCancelable(false)
                                            .setNegativeButton("ОК",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();

                                    saveRequestToHistory();
                                } else {
                                    showPB(false);
                                    Toast.makeText(getActivity(), "Сервер почему-то не смог создать заявку", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "onErrorResponse: ", e);
                                showPB(false);
                                Toast.makeText(getActivity(), "Произошла ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                            }
                        }

                        private void saveRequestToHistory() {
                            StringBuilder sb = new StringBuilder("INSERT INTO request (rqt_id, rqt_url, rqt_comment, rqt_imgsource, cli_fullname, cli_email, cli_phone, cli_address) VALUES (");
                            execPutFilter(sb, requestData.getRqt_id());
                            execPutFilter(sb, requestData.getuRL());
                            execPutFilter(sb, requestData.getComment());
                            execPutFilter(sb, requestData.getImagesource());
                            execPutFilter(sb, requestData.getFullName());
                            execPutFilter(sb, requestData.getEmail());
                            execPutFilter(sb, requestData.getPhone());
                            execPutFilter(sb, requestData.getAddress());
                            sb.append(");").append("");
                            sb.deleteCharAt(sb.length() - 4);
                            db.execSQL(sb.toString());
                        }
                    };
                default:
                    return null;
            }
        }

        private void execPutFilter(StringBuilder sb, Integer value) {
            if (value == null) sb.append(" null, ");
            else sb.append(" ").append(value).append(", ");
        }

        private void execPutFilter(StringBuilder sb, String value) {
            if (value == null) sb.append(" null, ");
            else sb.append(" '").append(value).append("', ");
        }

        private void mapPutFilter(Map<String, String> map, String key, String value) {
            if (value == null) return;
            map.put(key, value);
        }

        private void sbPutFilter(StringBuilder sb, String key, Integer value) {
            if (value == null) return;
            sb.append("\"").append(key).append("\":").append(value).append(",");
        }

        private void sbPutFilter(StringBuilder sb, String key, String value) {
            if (value == null) return;
            sb.append("\"").append(key).append("\":\"").append(value).append("\"").append(",");
        }

        private void sbPutFilter(StringBuilder sb, String key, Boolean value) {
            if (value == null) return;
            sb.append("\"").append(key).append("\":").append(value).append(",");
        }
    }

    private String metFilter(MaterialEditText met) {
        try {
            if (met.getText().length() != 0) {
                return met.getText().toString();
            } else return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String metFilter(MaterialMultiAutoCompleteTextView met) {
        try {
            if (met.getText().length() != 0) {
                return met.getText().toString();
            } else return null;
        } catch (Exception ex) {
            return null;
        }
    }
}

