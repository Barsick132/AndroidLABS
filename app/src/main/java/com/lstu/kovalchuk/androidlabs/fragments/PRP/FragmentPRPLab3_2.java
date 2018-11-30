package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lstu.kovalchuk.androidlabs.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

public class FragmentPRPLab3_2 extends Fragment {

    private static final String TAG = "FragmentPRPLab3";
    private static final int BLOCK_SIZE = 50000;
    private static int COUNT_BLOCKS;

    private Button btnSendText;
    private TextView tvTextInfo;

    public FragmentPRPLab3_2() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab3_2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        tvTextInfo = getView().findViewById(R.id.prp_lab3_2_TextInfo);
        tvTextInfo.setVisibility(View.GONE);

        // Читаем текстовый файл и разбиваем на блоки
        List<String> listBlocks = new ArrayList<>();
        try {
            InputStream is = getActivity().getAssets().open("text.txt");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            String text = new String(bytes);
            is.close();

            COUNT_BLOCKS = (int)Math.ceil(text.length()/(double)BLOCK_SIZE);
            for (int i=0; i<COUNT_BLOCKS-1; i++) {
                listBlocks.add(text.substring(BLOCK_SIZE * i, BLOCK_SIZE + BLOCK_SIZE * i));
            }
            listBlocks.add(text.substring(BLOCK_SIZE*(COUNT_BLOCKS-1)));
        } catch (IOException e) {
            Log.e(TAG, "onStart: " + e);
        }

        btnSendText = getView().findViewById(R.id.prp_lab3_2_SendText);
        btnSendText.setOnClickListener(view -> {
            Thread thread = new Thread(() -> {
                RedisAdapter ra = new RedisAdapter();
                try {
                    ra.setBlocks(listBlocks);
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Содержимое файла загружено! Ожидайте...");
                        tvTextInfo.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvTextInfo.setVisibility(View.VISIBLE);
                    });
                }catch (Exception ex){
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Не удалось загрузить содержимое файла");
                        tvTextInfo.setTextColor(Color.RED);
                        tvTextInfo.setVisibility(View.VISIBLE);
                    });
                    Log.e(TAG, "onStart: " + ex);
                }
            });
            thread.start();
        });
    }

    interface intRedisAdapter{
        Jedis jedis = new Jedis("192.168.0.2");
        void setBlocks(List<String> listBlocks);
    }

    class RedisAdapter implements intRedisAdapter{
        @Override
        public void setBlocks(List<String> listBlocks) {
            jedis.set("block_size", String.valueOf(BLOCK_SIZE));
            jedis.set("count_blocks", String.valueOf(COUNT_BLOCKS));
            for(int i=0; i<listBlocks.size(); i++){
                jedis.set("block" +  i, listBlocks.get(i));
                jedis.set("flag" + i, "false");
            }
        }
    }
}


