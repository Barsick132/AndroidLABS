package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lstu.kovalchuk.androidlabs.R;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.FragmentPRPLab3_1.Word;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FragmentPRPLab3_2 extends Fragment {

    private static final String TAG = "FragmentPRPLab3_2";
    private static final int BLOCK_SIZE = 10000;
    private static int COUNT_BLOCKS;

    private Button btnSendText;
    private LinearLayout llProgressBar;
    private LinearLayout llLayoutContent;
    private LinearLayout llResult;
    private TextView tvTextInfo;
    private TextView tvTextInfo2;
    private Button btnCancel;
    private Button btnWait;

    private RedisAdapter ra;

    public static final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    public static final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

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
        tvTextInfo2 = getView().findViewById(R.id.prp_lab3_2_TextInfo2);

        llProgressBar = getView().findViewById(R.id.prp_lab3_2_ProgressBar);
        llProgressBar.setVisibility(View.GONE);
        llLayoutContent = getView().findViewById(R.id.prp_lab3_2_LayoutContent);
        llResult = getView().findViewById(R.id.prp_lab3_2_layoutRes);
        layoutContentEnable(true);

        // Читаем текстовый файл и разбиваем на блоки
        List<String> listBlocks = new ArrayList<>();
        try {
            InputStream is = getActivity().getAssets().open("text.txt");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            String text = new String(bytes);
            is.close();

            COUNT_BLOCKS = (int) Math.ceil(text.length() / (double) BLOCK_SIZE);
            for (int i = 0; i < COUNT_BLOCKS - 1; i++) {
                listBlocks.add(text.substring(BLOCK_SIZE * i, BLOCK_SIZE + BLOCK_SIZE * i));
            }
            listBlocks.add(text.substring(BLOCK_SIZE * (COUNT_BLOCKS - 1)));
        } catch (IOException e) {
            Log.e(TAG, "onStart: " + e);
        }

        // Обработчик нажатия кнопки "Загрузить"
        btnSendText = getView().findViewById(R.id.prp_lab3_2_SendText);
        btnSendText.setOnClickListener(view -> {
            tvTextInfo.setVisibility(View.GONE);
            Thread thread = new Thread(() -> {
                ra = new RedisAdapter();
                try {
                    ra.setBlocks(listBlocks);
                    startResultHandler();
                    ra.diconnect();
                }
                catch (JedisConnectionException ex){
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Нет подключения к серверу");
                        tvTextInfo.setTextColor(Color.RED);
                        tvTextInfo.setVisibility(View.VISIBLE);
                        llProgressBar.setVisibility(View.GONE);
                        layoutContentEnable(true);
                    });
                    Log.e(TAG, "onStart: " + ex);
                }
                catch (Exception ex) {
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Не удалось загрузить файла или обработать результат");
                        tvTextInfo.setTextColor(Color.RED);
                        tvTextInfo.setVisibility(View.VISIBLE);
                        llProgressBar.setVisibility(View.GONE);
                        layoutContentEnable(true);
                    });
                    Log.e(TAG, "onStart: " + ex);
                }
            });
            thread.start();
        });

        // Обработчик нажатия кнопки "Ожидать"
        btnWait = getView().findViewById(R.id.prp_lab3_2_Wait);
        btnWait.setOnClickListener(view -> {
            tvTextInfo.setVisibility(View.GONE);
            Thread thread = new Thread(() -> {
                ra = new RedisAdapter();
                try {
                    if (ra.checkUplloadedOldTask()) {
                        startResultHandler();
                    } else {
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo.setText("Предыдущая задача не найдена");
                            tvTextInfo.setTextColor(Color.RED);
                            tvTextInfo.setVisibility(View.VISIBLE);
                        });
                    }
                    ra.diconnect();
                }
                catch (JedisConnectionException ex){
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Нет подключения к серверу");
                        tvTextInfo.setTextColor(Color.RED);
                        tvTextInfo.setVisibility(View.VISIBLE);
                        llProgressBar.setVisibility(View.GONE);
                        layoutContentEnable(true);
                    });
                    Log.e(TAG, "onStart: " + ex);
                }
                catch (Exception ex) {
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo.setText("Не удалось обработать результат");
                        tvTextInfo.setTextColor(Color.RED);
                        tvTextInfo.setVisibility(View.VISIBLE);
                    });
                    Log.e(TAG, "onStart: " + ex);
                }
            });
            thread.start();
        });

        // Обработчик нажатия кнопки "Отмена"
        btnCancel = getView().findViewById(R.id.prp_lab3_2_Cancel);
        btnCancel.setOnClickListener(view -> ra.close());
    }

    @Override
    public void onPause() {
        super.onPause();

        // Останавливаем поток ожидания при уходе с текущего фрагмента
        if (ra != null) {
            ra.diconnect();
            ra.close();
        }
    }

    // Функция блокировки содержимого главного Layout
    private void layoutContentEnable(boolean enable){
        if(enable) {
            for (int i = 0; i < llLayoutContent.getChildCount(); i++) {
                View view = llLayoutContent.getChildAt(i);
                view.setEnabled(true);
            }
        }else {
            for (int i = 0; i < llLayoutContent.getChildCount(); i++) {
                View view = llLayoutContent.getChildAt(i);
                view.setEnabled(false);
            }
        }
    }

    // Функция начала получения результата из БД
    private void startResultHandler() {
        getActivity().runOnUiThread(() -> {
            llProgressBar.setVisibility(View.VISIBLE);
            layoutContentEnable(false);
        });
        List<String> listResult = ra.waitResult();
        if (listResult != null) {
            ResultHandler(listResult);
            getActivity().runOnUiThread(() -> {
                tvTextInfo.setText("Выполнение задачи завершено!");
                tvTextInfo.setTextColor(Color.GREEN);
                tvTextInfo.setVisibility(View.VISIBLE);
                llProgressBar.setVisibility(View.GONE);
                layoutContentEnable(true);
            });
        } else {
            try {
                ra.diconnect();
                getActivity().runOnUiThread(() -> {
                    tvTextInfo.setText("Ожидание завершения задачи прервано");
                    tvTextInfo.setTextColor(Color.RED);
                    tvTextInfo.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.GONE);
                    layoutContentEnable(true);
                });
            }catch (NullPointerException ex){
                Log.e(TAG, "startResultHandler: " + ex.getMessage());
            }
        }
    }

    // Обработчик результата с выводом
    private void ResultHandler(List<String> listResultBlocks) {
        List<Word> listResult = new ArrayList<>();
        Gson gson = new Gson();
        for (int i = 0; i < listResultBlocks.size(); i++) {
            List<Word> wordList = gson.fromJson(listResultBlocks.get(i), new TypeToken<List<Word>>(){}.getType());
            for(int j=0; j<wordList.size(); j++){
                Word w = wordList.get(j);
                Predicate condition = sample -> ((Word) sample).getWord().equals(w.getWord());
                Collection ListSearchedObj = CollectionUtils.select(listResult, condition);
                if(ListSearchedObj.size()==0){
                    listResult.add(w);
                }else {
                    ((Word) ((ArrayList) ListSearchedObj).get(0)).addCountUp(w.getCount());
                }
                Log.d(TAG, "ResultHandler: block:" + i + "/" + listResultBlocks.size() + "; item: " + j + "/" + wordList.size());
            }
        }
        Collections.sort(listResult, (a, b) -> {
            if (a.getCount() < b.getCount()) return -1;
            if (a.getCount() > b.getCount()) return 1;
            return a.getWord().compareTo(b.getWord());
        });
        StringBuilder sbRes1 = new StringBuilder();
        StringBuilder sbRes2 = new StringBuilder();
        for(int i=listResult.size()-1; i>=0; i--){
            Word w = listResult.get(i);
            sbRes1.append(w.getWord()).append("\n");
            sbRes2.append(w.getCount()).append("\n");
        }
        getActivity().runOnUiThread(() -> {
            TextView textView1 = new TextView(getActivity());
            textView1.setText(sbRes1.toString());
            textView1.setTextColor(Color.BLACK);
            llResult.addView(textView1,layoutParams1);
            TextView textView2 = new TextView(getActivity());
            textView2.setText(sbRes2.toString());
            textView2.setTextColor(Color.BLACK);
            llResult.addView(textView2,layoutParams2);
        });
    }

    // Класс для работы с БД Redis
    private class RedisAdapter {
        private boolean close = false;
        Jedis jedis;

        // Конструктор подключения к Серверу
        private RedisAdapter(){
            jedis = new Jedis("127.0.0.1");
        }

        // Функция передачи блоков в БД
        private void setBlocks(List<String> listBlocks) {
            jedis.set("block_size", String.valueOf(BLOCK_SIZE));
            jedis.set("count_blocks", String.valueOf(COUNT_BLOCKS));
            jedis.set("worker_counter", "0");
            jedis.set("complete_counter", "0");
            jedis.set("oldTaskUploaded", "true");

            for (int i = 0; i < listBlocks.size(); i++) {
                jedis.set("block" + i, listBlocks.get(i));
                jedis.set("flag" + i, "false");
            }
        }

        // Функция ожидания результатов вычислений в БД
        @SuppressLint("SetTextI18n")
        private List<String> waitResult() {
            COUNT_BLOCKS = Integer.parseInt(jedis.get("count_blocks"));
            while (true) {
                int complete_counter = Integer.parseInt(jedis.get("complete_counter"));
                try {
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo2.setText("Рассчитано блоков: " + complete_counter + "/" + COUNT_BLOCKS);
                    });
                }catch (NullPointerException ex){
                    Log.e(TAG, "waitResult: "+ ex.getMessage());
                    close = true;
                }
                if (complete_counter == COUNT_BLOCKS) {
                    return getResult();
                } else {
                    try {
                        if (close) return null;
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Функция проверки наличия старой задачи в БД
        private boolean checkUplloadedOldTask() {
            try {
                return Boolean.parseBoolean(jedis.get("oldTaskUploaded"));
            }catch (NullPointerException ex){
                return false;
            }
        }

        // Функция получения результатов расчетов из БД
        private List<String> getResult() {
            List<String> list = new ArrayList<>(COUNT_BLOCKS);
            for (int i = 0; i < COUNT_BLOCKS; i++) {
                list.add(jedis.get("res" + i));
            }
            return list;
        }

        // Функция завершения потока ожидания результатов
        private void close() {
            close = true;
        }

        // Функция отключения от БД
        private void diconnect(){
            try {
                jedis.close();
                jedis.disconnect();
            }catch (JedisConnectionException ex){
                Log.e(TAG, "diconnect: " + ex.getMessage());
            }
        }
    }
}


