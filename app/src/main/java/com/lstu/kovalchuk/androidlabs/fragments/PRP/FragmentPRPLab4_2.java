package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.graphics.Color;
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
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lstu.kovalchuk.androidlabs.R;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class FragmentPRPLab4_2 extends Fragment {

    private static final String HOST = "192.168.0.2";
    private static final String TAG = "FragmentPRPLab4_2";
    private static final String CH_RECEIVE = "2", CH_SEND = "1";
    private static final int BL_S = 100000;

    private Button btnSendText;
    private LinearLayout llProgressBar;
    private LinearLayout llLayoutContent;
    private LinearLayout llResult;
    private TextView tvTextInfo;
    private TextView tvTextInfo2;
    private Button btnCancel;

    private FragmentActivity fragmentActivity;

    private RedisAdapter ra;
    private List<FragmentPRPLab3_1.Word> wl;

    public static final LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams
            (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    public static final LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams
            (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab4_2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentActivity = getActivity();

        tvTextInfo = getView().findViewById(R.id.prp_lab4_2_TextInfo);
        tvTextInfo.setVisibility(View.GONE);
        tvTextInfo2 = getView().findViewById(R.id.prp_lab4_2_TextInfo2);

        llProgressBar = getView().findViewById(R.id.prp_lab4_2_ProgressBar);
        llProgressBar.setVisibility(View.GONE);
        llLayoutContent = getView().findViewById(R.id.prp_lab4_2_LayoutContent);
        llResult = getView().findViewById(R.id.prp_lab4_2_layoutRes);
        layoutContentEnable(true);

        // Обработчик нажатия кнопки "Загрузить"
        btnSendText = getView().findViewById(R.id.prp_lab4_2_SendText);
        btnSendText.setOnClickListener(view -> {
            updateProgressBarUI(true);
            tvTextInfo.setVisibility(View.GONE);
            Thread thread = new Thread(() -> {
                try {
                    ra = new RedisAdapter(BL_S);
                    ra.connect();
                } catch (Exception ex) {
                    fragmentActivity.runOnUiThread(() -> {
                        if (!ex.getMessage().equals("java.net.SocketException: Socket is closed")) {
                            updateTextViewUI(tvTextInfo,
                                    "Не удалось подключиться к серверу",
                                    Color.RED,
                                    View.VISIBLE);
                        }
                        updateProgressBarUI(false);
                        Log.e(TAG, "onStart: " + ex.getMessage());
                    });
                }
            });
            thread.start();
        });

        // Обработчик нажатия кнопки "Отмена"
        btnCancel = getView().findViewById(R.id.prp_lab4_2_Cancel);
        btnCancel.setOnClickListener(view -> {
            ra.jedisPubSub.unsubscribe();
            updateProgressBarUI(false);
            updateTextViewUI(tvTextInfo, "Обработка текста была прервана",
                    Color.RED, View.VISIBLE);
        });

        if (wl != null) {
            new Thread(() -> {
                printResult(wl);
            }).start();
        } else {
            if (ra != null && ra.jedis.isConnected()) {
                Log.d(TAG, "onStart: jedis is Connected!");
                updateProgressBarUI(true);
                updateTextViewUI(tvTextInfo2, "Обработано " + ra.count_complete + " блоков из " + ra.COUNT_BLOCKS,
                        Color.WHITE, View.VISIBLE);
            }
        }
    }

    private void updateProgressBarUI(boolean show) {
        if (show) {
            llProgressBar.setVisibility(View.VISIBLE);
            layoutContentEnable(false);
        } else {
            llProgressBar.setVisibility(View.GONE);
            layoutContentEnable(true);
        }
    }

    private void printResult(List<FragmentPRPLab3_1.Word> wordList) {
        StringBuilder sbRes1 = new StringBuilder();
        StringBuilder sbRes2 = new StringBuilder();
        for (int i = wordList.size() - 1; i >= 0; i--) {
            FragmentPRPLab3_1.Word w = wordList.get(i);
            sbRes1.append(w.getWord()).append("\n");
            sbRes2.append(w.getCount()).append("\n");
        }

        fragmentActivity.runOnUiThread(() -> {
            TextView textView1 = new TextView(fragmentActivity);
            textView1.setText(sbRes1.toString());
            textView1.setTextColor(Color.BLACK);
            llResult.addView(textView1, layoutParams1);
            TextView textView2 = new TextView(fragmentActivity);
            textView2.setText(sbRes2.toString());
            textView2.setTextColor(Color.BLACK);
            llResult.addView(textView2, layoutParams2);
        });
    }

    private void updateTextViewUI(TextView view, String text, int color, int visibility) {
        view.setText(text);
        view.setTextColor(color);
        view.setVisibility(visibility);
    }

    // Функция блокировки содержимого главного Layout
    private void layoutContentEnable(boolean enable) {
        if (enable) {
            for (int i = 0; i < llLayoutContent.getChildCount(); i++) {
                View view = llLayoutContent.getChildAt(i);
                view.setEnabled(true);
            }
        } else {
            for (int i = 0; i < llLayoutContent.getChildCount(); i++) {
                View view = llLayoutContent.getChildAt(i);
                view.setEnabled(false);
            }
        }
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
        private Jedis jedis;
        private JedisPubSub jedisPubSub;
        private int COUNT_BLOCKS;
        private int BLOCK_SIZE;
        private List<Block> listBlocks;
        private MultiSet<String> multiSet;
        private int count_complete;

        private RedisAdapter(int block_size) {
            BLOCK_SIZE = block_size;
            multiSet = new HashMultiSet<>();
            count_complete = 0;

            jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    super.onMessage(channel, message);

                    Pattern regex = Pattern.compile("^UUID: (.+) blockReturn$");
                    Matcher matcher = regex.matcher(message);
                    if (matcher.matches() && matcher.group(1).length() == 36) {
                        Matcher finalMatcher = matcher;
                        new Thread(() -> returnBlock(finalMatcher.group(1))).start();
                        return;
                    }

                    regex = Pattern.compile("^UUID: (.+) Result: (.+)$");
                    matcher = regex.matcher(message);
                    if (matcher.matches() && matcher.group(1).length() == 36) {
                        Matcher finalMatcher = matcher;
                        new Thread(() -> addResult(finalMatcher.group(1), finalMatcher.group(2))).start();
                        return;
                    }

                    regex = Pattern.compile("^UUID: (.+)$");
                    matcher = regex.matcher(message);
                    if (matcher.matches() && matcher.group(1).length() == 36) {
                        sendBlock(matcher.group(1));
                    }
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    super.onUnsubscribe(channel, subscribedChannels);
                    disconnect(jedis);
                }
            };
            listBlocks = getTextBlocks();
        }

        private List<Block> getTextBlocks() {
            // Читаем текстовый файл и разбиваем на блоки
            List<Block> listBlocks = new ArrayList<>();
            try {
                InputStream is = fragmentActivity.getAssets().open("text.txt");
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                String text = new String(bytes);
                is.close();

                COUNT_BLOCKS = (int) Math.ceil(text.length() / (double) BLOCK_SIZE);
                for (int i = 0; i < COUNT_BLOCKS - 1; i++) {
                    listBlocks.add(new Block(text.substring(BLOCK_SIZE * i, BLOCK_SIZE + BLOCK_SIZE * i)));
                }
                listBlocks.add(new Block(text.substring(BLOCK_SIZE * (COUNT_BLOCKS - 1))));
                return listBlocks;
            } catch (IOException e) {
                Log.e(TAG, "onStart: " + e);
                return null;
            }
        }

        private void connect() {
            jedis = new Jedis(HOST);
            jedis.connect();

            wl = null;
            fragmentActivity.runOnUiThread(() -> {
                updateTextViewUI(tvTextInfo2, "Обработано 0 блоков из " + COUNT_BLOCKS,
                        Color.WHITE, View.VISIBLE);
            });

            jedis.subscribe(jedisPubSub, CH_RECEIVE);
        }

        private void sendBlock(String uuid) {
            Block b = null;
            for (Block block : listBlocks) {
                if (block.uuid == null) {
                    block.uuid = uuid;
                    b = block;
                    break;
                }
            }
            try (Jedis jedis = new Jedis(HOST)) {
                jedis.connect();
                if (b != null)
                    jedis.publish(CH_SEND, uuid + ": Text: " + b.text);
                else jedis.publish(CH_SEND, uuid + ": Not Blocks");
            } catch (Exception ex) {
                Log.e(TAG, "sendBlock: " + ex.getMessage());
            }
        }

        private void returnBlock(String uuid) {
            for (Block block : listBlocks) {
                if (block.uuid.equals(uuid)) {
                    block.uuid = null;
                    break;
                }
            }
        }

        private void addResult(String uuid, String result) {
            for (Block block : listBlocks) {
                if (block.uuid.equals(uuid)) {
                    block.complete = true;
                    count_complete++;
                    break;
                }
            }

            fragmentActivity.runOnUiThread(() -> {
                updateTextViewUI(tvTextInfo2, "Обработано " + count_complete + " блоков из " + COUNT_BLOCKS,
                        Color.WHITE, View.VISIBLE);
            });

            Gson gson = new Gson();
            List<FragmentPRPLab3_1.Word> wordList = gson.fromJson(result, new TypeToken<List<FragmentPRPLab3_1.Word>>() {
            }.getType());
            for (FragmentPRPLab3_1.Word w : wordList) {
                multiSet.add(w.getWord(), w.getCount());
            }
            if (count_complete == COUNT_BLOCKS) {
                wl = convertMultiSetToListWord(multiSet);

                printResult(wl);

                jedisPubSub.unsubscribe();
            }
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
    }

    private class Block {
        private String text;
        private String uuid;
        private Boolean complete;

        private Block(String block) {
            this.text = block;
            uuid = null;
            complete = false;
        }
    }
}
