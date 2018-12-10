package com.lstu.kovalchuk.androidlabs.fragments.PRP;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lstu.kovalchuk.androidlabs.R;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import static com.lstu.kovalchuk.androidlabs.R.string.Start;

public class FragmentPRPLab3_1 extends Fragment {

    private static final String TAG = "FragmentPRPLab3_1";

    private static int ID;
    private static int COUNT_PROC;
    private Integer complete_proc;
    private Integer error_complete_proc;

    private Button btnStart;
    private TextView tvTextInfo0;
    private TextView tvTextInfo1;
    private TextView tvTextInfo2;
    private LinearLayout llListErrors;
    private ProgressBar pbLoading;
    private int pbCounter;

    private List<AsyncTask> taskList;
    public static final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private boolean task_started;

    public FragmentPRPLab3_1() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab3_1, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();

        ID = -1;
        taskList = null;
        task_started = false;
        llListErrors = getView().findViewById(R.id.prp_lab3_1_ListError);
        tvTextInfo0 = getView().findViewById(R.id.prp_lab3_1_TextInfo0);
        tvTextInfo1 = getView().findViewById(R.id.prp_lab3_1_TextInfo1);
        tvTextInfo2 = getView().findViewById(R.id.prp_lab3_1_TextInfo2);
        tvTextInfo1.setVisibility(View.GONE);
        pbLoading = getView().findViewById(R.id.prp_lab3_1_progressBar);
        pbLoading.setProgress(0);

        // Обработчик кнопки запуска расчетов
        btnStart = getView().findViewById(R.id.prp_lab3_1_Start);
        btnStart.setOnClickListener(view -> {
            if (!task_started) {
                if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                Thread thread = new Thread(() -> {
                    RedisAdapter ra = new RedisAdapter();
                    try {
                        String block = ra.getBlock();
                        taskList = null;
                        if (block != null) {
                            String[] split = block.split("\\s+");
                            getActivity().runOnUiThread(() -> startCalculate(split));
                        } else {
                            getActivity().runOnUiThread(() -> {
                                tvTextInfo2.setText("Нет доступных для обработки блоков");
                                tvTextInfo2.setTextColor(getResources().getColor(R.color.colorPrimary));
                                tvTextInfo2.setVisibility(View.VISIBLE);
                                task_started = false;
                                btnStart.setText(getResources().getText(R.string.Start));
                                tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                                tvTextInfo1.setVisibility(View.GONE);
                            });
                            ra.diconnect();
                        }
                    } catch (JedisConnectionException ex) {
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo2.setText("Ошибка подключения к серверу");
                            tvTextInfo2.setTextColor(Color.RED);
                            tvTextInfo2.setVisibility(View.VISIBLE);
                            task_started = false;
                            btnStart.setText(getResources().getText(R.string.Start));
                            tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                            tvTextInfo1.setVisibility(View.GONE);
                        });
                        Log.e(TAG, "onStart: " + ex);
                    }
                });
                thread.start();
                task_started = true;
                btnStart.setText("Stop");
                tvTextInfo0.setText(getResources().getText(R.string.Info_for_Stop));
            }else {
                if (taskList == null) return;
                for (int i = 0; i < taskList.size(); i++) {
                    taskList.get(i).cancel(true);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Прерываем расчеты, если уходим с данного фрагмента
        if (taskList == null) return;
        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).cancel(true);
        }
    }

    // Запуск потокв для подсчета слов
    private void startCalculate(String[] split) {
        getCountProc();

        pbCounter = 0;
        pbLoading.setMax(split.length);
        pbLoading.setProgress(pbCounter);

        List<Word> wordList = Collections.synchronizedList(new ArrayList<>());
        tvTextInfo2.setText("Начинаем вычисления. Ожидайте...");
        tvTextInfo2.setTextColor(getResources().getColor(R.color.colorBlack));
        tvTextInfo2.setVisibility(View.VISIBLE);
        tvTextInfo1.setVisibility(View.VISIBLE);
        Log.d(TAG, "startCalculate: Start thread");
        complete_proc = 0;
        error_complete_proc = 0;
        taskList = new ArrayList<>();
        for (int i = 0; i < COUNT_PROC; i++) {
            if (COUNT_PROC > 1) {
                List<String[]> listArraysWords = getListArraysWords(split);
                // Вызываем в каждом потоке функцию размытия
                String[] newSplit = listArraysWords.get(i);
                taskList.add(new AsyncCalculate(newSplit, wordList, i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
            } else {
                taskList.add(new AsyncCalculate(split, wordList, i).execute());
            }
        }

    }

    // Разбивка массива слов на несколько равных массивов (по кол-ву потоков)
    private List<String[]> getListArraysWords(String[] split) {
        int step = split.length / COUNT_PROC;
        List<String[]> listArraysWords = new ArrayList<>();
        for (int i = 0; i < COUNT_PROC; i++) {
            int size_arrays = step;
            if (i == COUNT_PROC - 1) size_arrays = split.length - i * step;
            String[] newSplit = new String[size_arrays];
            System.arraycopy(split, i * step, newSplit, 0, size_arrays);
            listArraysWords.add(newSplit);
        }
        return listArraysWords;
    }

    // Определитель оптимального кол-ва потоков для обработки
    private void getCountProc() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors > 4) {
            COUNT_PROC = 4;
        } else {
            if (processors > 1) {
                COUNT_PROC = Runtime.getRuntime().availableProcessors() - 1;
            } else {
                COUNT_PROC = 1;
            }
        }
    }

    // Асинхронный класс для выполнения подсчетов
    @SuppressLint("StaticFieldLeak")
    class AsyncCalculate extends AsyncTask<Void, Void, Void> {

        private String[] split;
        private List<Word> listWords;
        private int id;


        AsyncCalculate(String[] split, List<Word> listWords, int id) {
            this.split = split;
            this.listWords = listWords;
            this.id = id;
        }

        // Счетчик слов
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (String str : split) {
                    if (isCancelled()) return null;
                    synchronized (listWords) {
                        String str1 = str.toLowerCase();
                        Predicate condition = sample -> ((Word) sample).word.equals(str1);
                        Collection ListSearchedObj = CollectionUtils.select(listWords, condition);

                        if (ListSearchedObj.size() == 0) {
                            listWords.listIterator(0).add(new Word(str1, 1));
                        } else {
                            ((Word) ((ArrayList) ListSearchedObj).get(0)).incCountUp();
                        }
                        publishProgress();
                    }
                }
                complete_proc++;
                Log.d(TAG, "onStart: Complete thread " + id + "/" + COUNT_PROC);
            } catch (Exception ex) {
                Log.e(TAG, "onStart: " + ex);
                getActivity().runOnUiThread(() -> {
                    TextView tvError = new TextView(getActivity());
                    tvError.setText("В потоке " + id + " произошла ошибка.");
                    tvError.setTextColor(getResources().getColor(R.color.colorRed));
                    llListErrors.addView(tvError, layoutParams);
                });
                error_complete_proc++;
            }
            return null;
        }

        // Обновление шкалы загрузки
        @Override
        protected void onProgressUpdate(Void... values) {
            pbCounter++;
            pbLoading.setProgress(pbCounter);
        }

        // Обработчик завершения потока
        @Override
        protected void onPostExecute(Void aVoid) {
            TextView tvError = new TextView(getActivity());
            tvError.setText("Поток " + id + " завершен. Всего " + COUNT_PROC);
            tvError.setTextColor(getResources().getColor(R.color.colorBlack));
            llListErrors.addView(tvError, layoutParams);

            // Если все потоки завершены, передаем результаты в БД
            if (complete_proc == COUNT_PROC) {
                Collections.sort(listWords, (a, b) -> {
                    if (a.getCount() < b.getCount()) return -1;
                    if (a.getCount() > b.getCount()) return 1;
                    return a.word.compareTo(b.word);
                });

                Thread thread = new Thread(() -> {
                    RedisAdapter ra = new RedisAdapter();
                    try {
                        Gson gson = new Gson();
                        Log.d(TAG, "onPostExecute: data uploaded. ID: " + ID);
                        ra.setResult(gson.toJson(listWords));
                        ra.diconnect();
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo2.setText("Успешное завершение всех расчетов!");
                            tvTextInfo2.setTextColor(getResources().getColor(R.color.colorGreen));
                            tvTextInfo2.setVisibility(View.VISIBLE);
                            btnStart.setText(getResources().getText(Start));
                            tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                            pbLoading.setProgress(0);
                        });
                        task_started = false;
                    } catch (JedisConnectionException ex) {
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo2.setVisibility(View.VISIBLE);
                            tvTextInfo2.setTextColor(Color.RED);
                            tvTextInfo2.setText("Подключение к серверу потеряно");
                            if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                            tvTextInfo1.setVisibility(View.GONE);
                            btnStart.setText(getResources().getText(Start));
                            tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                            pbLoading.setProgress(0);
                        });
                        task_started = false;
                        Log.e(TAG, "onPostExecute: " + ex.getMessage());
                    }
                });
                thread.start();
            }

            // Если были ошибки при расчетах, то открываем доступ к блоку в БД другим Подписчикам
            if (error_complete_proc != 0 && error_complete_proc + complete_proc == COUNT_PROC) {
                tvTextInfo2.setText("Расчеты завершены с ошибкой...");
                tvTextInfo2.setTextColor(getResources().getColor(R.color.colorRed));
                Thread thread = new Thread(() -> {
                    RedisAdapter ra = new RedisAdapter();
                    try {
                        ra.returnBlock();
                        ra.diconnect();
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo1.setText("Блок " + ID + "разблокирован для других пользователей");
                            tvTextInfo1.setTextColor(getResources().getColor(R.color.colorPrimary));
                            tvTextInfo1.setVisibility(View.VISIBLE);
                            tvTextInfo2.setVisibility(View.GONE);
                            if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                            btnStart.setText(getResources().getText(Start));
                            tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                            pbLoading.setProgress(0);
                        });
                        task_started = false;
                    } catch (JedisConnectionException ex) {
                        getActivity().runOnUiThread(() -> {
                            tvTextInfo2.setVisibility(View.VISIBLE);
                            tvTextInfo2.setTextColor(Color.RED);
                            tvTextInfo2.setText("Подключение к серверу потеряно");
                            if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                            tvTextInfo1.setVisibility(View.GONE);
                            btnStart.setText(getResources().getText(Start));
                            tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                            pbLoading.setProgress(0);
                        });
                        task_started = false;
                    }
                });
                thread.start();
            }
        }

        // Обработчик прерывания потока
        @Override
        protected void onCancelled() {
            super.onCancelled();
            error_complete_proc++;

            // Если все потоки завершены, то открываем доступ в БД для других потоков
            if (error_complete_proc + complete_proc == COUNT_PROC) {
                Thread thread = new Thread(() -> {
                    RedisAdapter ra = new RedisAdapter();
                    try {
                        ra.returnBlock();
                        ra.diconnect();
                        try {
                            getActivity().runOnUiThread(() -> {
                                btnStart.setText(getResources().getText(Start));
                                tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                                tvTextInfo1.setVisibility(View.GONE);
                                tvTextInfo2.setVisibility(View.GONE);
                                if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                                pbLoading.setProgress(0);
                            });
                        }catch (NullPointerException ex){
                            Log.e(TAG, "onCancelled: " + ex.getMessage());
                        }
                        task_started = false;
                    } catch (JedisConnectionException ex) {
                        try {
                            getActivity().runOnUiThread(() -> {
                                btnStart.setText(getResources().getText(Start));
                                tvTextInfo0.setText(getResources().getText(R.string.Info_for_Start));
                                tvTextInfo1.setVisibility(View.GONE);
                                tvTextInfo2.setVisibility(View.VISIBLE);
                                tvTextInfo2.setTextColor(Color.RED);
                                tvTextInfo2.setText("Подключение к серверу потеряно");
                                if (llListErrors.getChildCount() > 0) llListErrors.removeAllViews();
                                pbLoading.setProgress(0);
                            });
                        }catch (NullPointerException npex){
                            Log.e(TAG, "onCancelled: " + npex.getMessage());
                        }
                        task_started = false;
                        Log.e(TAG, "onCancelled: " + ex.getMessage());
                    }
                });
                thread.start();
            }
        }
    }

    // Класс для работы с БД Redis
    private class RedisAdapter {
        private Jedis jedis;

        // Конструктор подключения к Серверу
        private RedisAdapter() {
            jedis = new Jedis("192.168.0.2");
        }

        // Функция получения блока слов из БД
        private String getBlock() {
            List<String> res = new ArrayList<>();
            int BLOCK_SIZE = Integer.parseInt(jedis.get("block_size"));
            int COUNT_BLOCKS = Integer.parseInt(jedis.get("count_blocks"));
            if (COUNT_BLOCKS == 0) return null;
            for (int i = 0; i < COUNT_BLOCKS; i++) {
                boolean taken = Boolean.parseBoolean(jedis.get("flag" + i));
                if (!taken) {
                    ID = i;
                    getActivity().runOnUiThread(() -> {
                        tvTextInfo1.setText("В этой сессии ваш ID: " + ID);
                        tvTextInfo1.setTextColor(getResources().getColor(R.color.colorPrimary));
                    });
                    int count_worker = Integer.parseInt(jedis.get("worker_counter"));
                    jedis.set("worker_counter", String.valueOf(count_worker + 1));
                    jedis.set("flag" + i, "true");
                    return jedis.get("block" + i);
                }
            }
            getActivity().runOnUiThread(() -> {
                tvTextInfo2.setText("Нет доступных для обработки блоков");
                tvTextInfo2.setTextColor(getResources().getColor(R.color.colorPrimary));
            });
            return null;
        }

        // Функция снятия блокировки блока в БД
        private void returnBlock() {
            int count_worker = Integer.parseInt(jedis.get("worker_counter"));
            jedis.set("worker_counter", String.valueOf(count_worker - 1));
            jedis.set("flag" + ID, "false");
        }

        // Функция передачи результата в БД
        private void setResult(String res) {
            jedis.set("res" + ID, res);
            int count_worker = Integer.parseInt(jedis.get("worker_counter"));
            jedis.set("worker_counter", String.valueOf(count_worker - 1));
            int count_complete = Integer.parseInt(jedis.get("complete_counter"));
            jedis.set("complete_counter", String.valueOf(count_complete + 1));
            ID = -1;
        }

        // Функция отключения от БД
        private void diconnect() {
            try {
                jedis.close();
                jedis.disconnect();
            } catch (JedisConnectionException ex) {
                Log.e(TAG, "diconnect: " + ex.getMessage());
            }
        }
    }

    // Класс для хранения слова и кол-ва его упоминаний
    public class Word {
        private String word;
        private int count;

        Word(String word, int count) {
            this.word = word;
            this.count = count;
        }

        String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        void incCountUp() {
            count++;
        }

        void addCountUp(int number) {
            count+=number;
        }
    }
}
