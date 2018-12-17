package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class FragmentPRPLab4_1 extends Fragment {

    private static final String HOST = "192.168.0.2";
    private static final String TAG = "FragmentPRPLab4_1";
    private static final String CH_RECEIVE = "1", CH_SEND = "2";

    private static int COUNT_PROC;
    private int error_complete_proc;
    private int complete_proc;
    private List<AsyncTask<String[], Void, Boolean>> taskList;

    private MultiSet<String> multiSet;

    private LinearLayout llListErrors;
    private TextView tvTextInfo0;
    private TextView tvTextInfo1;
    private TextView tvTextInfo2;
    private ProgressBar pbLoading;
    private int pbCounter;
    private Button btnSendText;

    private FragmentActivity fragmentActivity;

    public static final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
            (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab4_1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentActivity = getActivity();

        llListErrors = getView().findViewById(R.id.prp_lab4_1_ListError);
        tvTextInfo0 = getView().findViewById(R.id.prp_lab4_1_TextInfo0);
        tvTextInfo1 = getView().findViewById(R.id.prp_lab4_1_TextInfo1);
        tvTextInfo2 = getView().findViewById(R.id.prp_lab4_1_TextInfo2);
        tvTextInfo1.setVisibility(View.GONE);
        pbLoading = getView().findViewById(R.id.prp_lab4_1_progressBar);
        pbLoading.setProgress(0);

        getCountProc();

        btnSendText = getView().findViewById(R.id.prp_lab4_1_Start);
        btnSendText.setOnClickListener(view -> {
            if (llListErrors.getChildCount() != 0) llListErrors.removeAllViews();
            Thread thread = new Thread(() -> {
                try {
                    fragmentActivity.runOnUiThread(() -> {
                        tvTextInfo2.setVisibility(View.GONE);
                    });
                    RedisAdapter ra = new RedisAdapter();
                    ra.connect();
                } catch (Exception ex) {

                    fragmentActivity.runOnUiThread(() -> {
                        if (!ex.getMessage().equals("java.net.SocketException: Socket is closed")) {
                            updateTextViewUI(tvTextInfo2,
                                    "Не удалось подключиться к серверу",
                                    Color.RED,
                                    View.VISIBLE);
                        }
                        Log.e(TAG, "onStart: " + ex.getMessage());
                    });
                }
            });
            thread.start();
        });
    }

    private void updateTextViewUI(TextView view, String text, int color, int visibility) {
        view.setText(text);
        view.setTextColor(color);
        view.setVisibility(visibility);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (taskList != null) {
            for (AsyncTask task : taskList) {
                task.cancel(true);
            }
        }
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

    private List<FragmentPRPLab3_1.Word> convertMultiSetToListWord(MultiSet<String> multiSet) {
        List<FragmentPRPLab3_1.Word> wordList = new ArrayList<>();
        for (String w : multiSet.uniqueSet()) {
            int count = multiSet.getCount(w);
            wordList.add(new FragmentPRPLab3_1.Word(w, count));
        }
        Collections.sort(wordList, (a, b) -> {
            if (a.getCount() < b.getCount()) return -1;
            if (a.getCount() > b.getCount()) return 1;
            return a.getWord().compareTo(b.getWord());
        });
        return wordList;
    }

    private class RedisAdapter {
        private UUID uuid;
        private JedisPubSub jedisPubSub;
        private Jedis jedis;
        private Boolean timeoutFlag;

        private RedisAdapter() {
            jedisPubSub = new JedisPubSub() {
                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    super.onSubscribe(channel, subscribedChannels);

                    try (Jedis jedis = new Jedis(HOST)) {
                        jedis.connect();
                        jedis.publish(CH_SEND, "UUID: " + uuid.toString());
                    } catch (Exception ex) {
                        jedisPubSub.unsubscribe();
                        Log.e(TAG, "onSubscribe: " + ex.getMessage());
                    }
                }

                @Override
                public void onMessage(String channel, String message) {
                    super.onMessage(channel, message);
                    Pattern regex = Pattern.compile("^(.+): Not Blocks$");
                    Matcher matcher = regex.matcher(message);
                    if (matcher.matches() && matcher.group(1).equals(uuid.toString())) {
                        timeoutFlag = false;
                        fragmentActivity.runOnUiThread(() -> {
                            updateTextViewUI(tvTextInfo2, "Нет доступных для обработки блоков",
                                    getResources().getColor(R.color.colorPrimary), View.VISIBLE);
                        });
                        jedisPubSub.unsubscribe();
                        Log.d(TAG, "onMessage: нет доступных для обработки блоков");
                        return;
                    }
                    regex = Pattern.compile("^(.+): Text: (.+)$");
                    matcher = regex.matcher(message.replaceAll("\\s+", " "));
                    if (matcher.matches() && matcher.group(1).equals(uuid.toString())) {
                        timeoutFlag = false;
                        startCalculate(matcher.group(2));
                    }
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    super.onUnsubscribe(channel, subscribedChannels);
                    disconnect(jedis);
                }
            };
        }

        private void connect() {
            timeoutFlag = true;
            jedis = new Jedis(HOST);
            jedis.connect();
            uuid = UUID.randomUUID();
            new Thread(() ->
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (timeoutFlag) {
                                fragmentActivity.runOnUiThread(() -> {
                                    updateTextViewUI(tvTextInfo2, "Нет ответа от мастера",
                                            Color.RED, View.VISIBLE);
                                });
                                jedisPubSub.unsubscribe();
                            }
                        }
                    }, 3000)).start();
            jedis.subscribe(jedisPubSub, CH_RECEIVE);
        }

        private void startCalculate(String data) {
            String[] arrayWords = data.split("\\W+|_|\\d+");

            pbCounter = 0;
            pbLoading.setMax(arrayWords.length);
            pbLoading.setProgress(pbCounter);

            complete_proc = 0;
            error_complete_proc = 0;
            taskList = new ArrayList<>();
            multiSet = new HashMultiSet<>();
            if (COUNT_PROC > 1) {
                List<String[]> listArrayWords = getListArraysWords(arrayWords);
                for (int i = 0; i < COUNT_PROC; i++) {
                    // Вызываем в каждом потоке функцию размытия
                    String[] arrayWordsBlock = listArrayWords.get(i);
                    taskList.add(new AsyncCalculate(arrayWordsBlock, i + 1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
                }
            } else {
                taskList.add(new AsyncCalculate(arrayWords, 1).execute());
            }
        }

        private void sendResult(String result) {
            try (Jedis jedis = new Jedis(HOST)) {
                jedis.publish(CH_SEND, "UUID: " + uuid.toString() + " Result: " + result);
            } catch (Exception ex) {
                Log.e(TAG, "sendResult: " + ex.getMessage());
            }
        }

        private void returnBlock() {
            try (Jedis jedis = new Jedis(HOST)) {
                jedis.connect();
                jedis.publish(CH_SEND, "UUID: " + uuid.toString() + " blockReturn");
            } catch (Exception ex) {
                Log.e(TAG, "onCancelled: " + ex.getMessage());
            }
            jedisPubSub.unsubscribe();
        }

        private void disconnect(Jedis jedis) {
            try {
                if (jedis != null) {
                    jedis.close();
                    jedis.disconnect();
                }
            } catch (Exception ex) {
                Log.e(TAG, "disconnect: " + ex.getMessage());
            }
        }

        @SuppressLint("StaticFieldLeak")
        class AsyncCalculate extends AsyncTask<String[], Void, Boolean> {
            private String[] arrayWords;
            private int id;

            AsyncCalculate(String[] arrayWords, int id) {
                this.id = id;
                this.arrayWords = arrayWords;
            }

            @Override
            protected Boolean doInBackground(String[]... strings) {
                try {
                    for (String arrayWord : arrayWords) {
                        String word = arrayWord.toLowerCase();
                        if(word.equals("")) continue;
                        multiSet.add(word);
                        onProgressUpdate();
                        if (isCancelled()) return false;
                    }
                    return true;
                } catch (Exception ex) {
                    Log.e(TAG, "doInBackground: " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                fragmentActivity.runOnUiThread(() -> {
                    pbCounter++;
                    pbLoading.setProgress(pbCounter);
                });
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);

                if (aBoolean) {
                    complete_proc++;
                    fragmentActivity.runOnUiThread(() -> {
                        TextView tvError = new TextView(fragmentActivity);
                        tvError.setText("Поток " + id + " завершен успешно. Всего: " + COUNT_PROC);
                        tvError.setTextColor(Color.BLACK);
                        llListErrors.addView(tvError, layoutParams);
                    });
                } else {
                    error_complete_proc++;
                    fragmentActivity.runOnUiThread(() -> {
                        TextView tvError = new TextView(fragmentActivity);
                        tvError.setText("В потоке " + id + " произошла ошибка. Всего: " + COUNT_PROC);
                        tvError.setTextColor(Color.RED);
                        llListErrors.addView(tvError, layoutParams);
                    });
                }

                if (error_complete_proc == 0 && complete_proc == COUNT_PROC) {
                    new Thread(() -> {
                        List<FragmentPRPLab3_1.Word> wordList = convertMultiSetToListWord(multiSet);
                        Gson gson = new Gson();
                        sendResult(gson.toJson(wordList));
                        jedisPubSub.unsubscribe();
                    }).start();
                    fragmentActivity.runOnUiThread(() -> {
                        updateTextViewUI(tvTextInfo2, "Успешное завершение всех расчетов!",
                                Color.GREEN, View.VISIBLE);
                        pbLoading.setProgress(0);
                    });
                }
                if (error_complete_proc != 0 && complete_proc + error_complete_proc == COUNT_PROC) {
                    fragmentActivity.runOnUiThread(() -> {
                        updateTextViewUI(tvTextInfo2, "Не удалось посчитать все блоки",
                                Color.RED, View.VISIBLE);
                        pbLoading.setProgress(0);
                    });
                    new Thread(() -> returnBlock()).start();
                }
            }

            @Override
            protected void onCancelled(Boolean aBoolean) {
                super.onCancelled(aBoolean);

                error_complete_proc++;
                fragmentActivity.runOnUiThread(() -> {
                    TextView tvError = new TextView(fragmentActivity);
                    tvError.setText("Поток " + id + " принудительно завершен. Всего: " + COUNT_PROC);
                    tvError.setTextColor(Color.RED);
                    llListErrors.addView(tvError, layoutParams);
                });

                if (error_complete_proc != 0 && complete_proc + error_complete_proc == COUNT_PROC) {
                    fragmentActivity.runOnUiThread(() -> {
                        updateTextViewUI(tvTextInfo2, "Не удалось посчитать все блоки",
                                Color.RED, View.VISIBLE);
                        pbLoading.setProgress(0);
                    });
                    new Thread(() -> returnBlock()).start();
                }
            }
        }
    }
}
