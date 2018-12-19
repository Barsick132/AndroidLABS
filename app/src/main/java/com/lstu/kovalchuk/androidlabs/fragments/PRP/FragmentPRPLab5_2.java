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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lstu.kovalchuk.androidlabs.R;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.SocketLib.SocketAdapter;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.SocketLib.SocketPubSub;
import com.neovisionaries.ws.client.WebSocketException;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentPRPLab5_2 extends Fragment {
    private static final String HOST = "192.168.0.2";
    private static final String TAG = "FragmentPRPLab5_2";
    private static final String CH_RECEIVE = "2", CH_SEND = "1";
    private static final int BL_S = 100000;

    private Button btnSendText;
    private LinearLayout llProgressBar;
    private LinearLayout llLayoutContent;
    private LinearLayout llResult;
    private TextView tvTextInfo;
    private TextView tvTextInfo2;
    private ProgressBar pbFast;

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
        return inflater.inflate(R.layout.fragment_prplab5_2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentActivity = getActivity();

        pbFast = getView().findViewById(R.id.prp_lab5_2_FastPB);
        pbFast.setVisibility(View.GONE);
        tvTextInfo = getView().findViewById(R.id.prp_lab5_2_TextInfo);
        tvTextInfo.setVisibility(View.GONE);
        tvTextInfo2 = getView().findViewById(R.id.prp_lab5_2_TextInfo2);

        llProgressBar = getView().findViewById(R.id.prp_lab5_2_ProgressBar);
        llProgressBar.setVisibility(View.GONE);
        llLayoutContent = getView().findViewById(R.id.prp_lab5_2_LayoutContent);
        llResult = getView().findViewById(R.id.prp_lab5_2_layoutRes);
        layoutContentEnable(true);

        // Обработчик нажатия кнопки "Загрузить"
        btnSendText = getView().findViewById(R.id.prp_lab5_2_SendText);
        btnSendText.setOnClickListener(view -> {
            updateProgressBarUI(true);
            tvTextInfo.setVisibility(View.GONE);
            Thread thread = new Thread(() -> {
                try {
                    ra = new RedisAdapter(BL_S);
                    ra.connect();
                } catch (WebSocketException conEx) {
                    fragmentActivity.runOnUiThread(() -> {
                        updateTextViewUI(tvTextInfo,
                                "Не удалось подключиться к серверу",
                                Color.RED,
                                View.VISIBLE);
                        updateProgressBarUI(false);
                        Log.e(TAG, "onStart: " + conEx.getMessage());
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "onStart: неизвестная ошибка");
                }
            });
            thread.start();
        });


        // Обработчик нажатия кнопки "Отмена"
        Button btnCancel = getView().findViewById(R.id.prp_lab5_2_Cancel);
        btnCancel.setOnClickListener(view -> {
            ra.socketPubSub.unsubscribe();
            updateProgressBarUI(false);
            updateTextViewUI(tvTextInfo, "Обработка текста была прервана",
                    Color.RED, View.VISIBLE);
        });

        if (wl != null) {
            new Thread(() -> {
                printResult(wl);
            }).start();
        } else {
            if (ra != null && ra.socketAdapter.isConnected()) {
                Log.d(TAG, "onStart: socketAdapter is Connected!");
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

        fragmentActivity.runOnUiThread(() -> {
            pbFast.setVisibility(View.VISIBLE);
            btnSendText.setVisibility(View.GONE);
        });

        StringBuilder sbRes1 = new StringBuilder();
        StringBuilder sbRes2 = new StringBuilder();
        for (int i = wordList.size() - 1; i >= 0; i--) {
            FragmentPRPLab3_1.Word w = wordList.get(i);
            sbRes1.append(w.getWord()).append("\n");
            sbRes2.append(w.getCount()).append("\n");
        }

        fragmentActivity.runOnUiThread(() -> {
            pbFast.setVisibility(View.GONE);
            btnSendText.setVisibility(View.VISIBLE);

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
        private SocketAdapter socketAdapter;
        private SocketPubSub socketPubSub;
        private int COUNT_BLOCKS;
        private int BLOCK_SIZE;
        private List<FragmentPRPLab4_2.Block> listBlocks;
        private MultiSet<String> multiSet;
        private int count_complete;

        private RedisAdapter(int block_size) {
            BLOCK_SIZE = block_size;
            multiSet = new HashMultiSet<>();
            count_complete = 0;

            socketPubSub = new SocketPubSub() {
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
                public void onUnsubscribe(String channel) {
                    super.onUnsubscribe(channel);
                    disconnect(socketAdapter);
                }
            };
            listBlocks = getTextBlocks();
        }

        private List<FragmentPRPLab4_2.Block> getTextBlocks() {
            // Читаем текстовый файл и разбиваем на блоки
            List<FragmentPRPLab4_2.Block> listBlocks = new ArrayList<>();
            try {
                InputStream is = fragmentActivity.getAssets().open("text.txt");
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                String text = new String(bytes);
                is.close();

                COUNT_BLOCKS = (int) Math.ceil(text.length() / (double) BLOCK_SIZE);
                for (int i = 0; i < COUNT_BLOCKS - 1; i++) {
                    listBlocks.add(new FragmentPRPLab4_2.Block(text.substring(BLOCK_SIZE * i, BLOCK_SIZE + BLOCK_SIZE * i)));
                }
                listBlocks.add(new FragmentPRPLab4_2.Block(text.substring(BLOCK_SIZE * (COUNT_BLOCKS - 1))));
                return listBlocks;
            } catch (IOException e) {
                Log.e(TAG, "onStart: " + e);
                return null;
            }
        }

        private void connect() throws IOException, WebSocketException {
            socketAdapter = new SocketAdapter(HOST);

            wl = null;
            fragmentActivity.runOnUiThread(() -> {
                updateTextViewUI(tvTextInfo2, "Обработано 0 блоков из " + COUNT_BLOCKS,
                        Color.WHITE, View.VISIBLE);
            });
            socketAdapter.subscribe(socketPubSub, CH_RECEIVE);

        }

        private void sendBlock(String uuid) {
            FragmentPRPLab4_2.Block b = null;
            for (FragmentPRPLab4_2.Block block : listBlocks) {
                if (block.getUuid() == null) {
                    block.setUuid(uuid);
                    b = block;
                    break;
                }
            }

            if (b != null)
                socketAdapter.publish(CH_SEND, uuid + ": Text: " + b.getText());
            else socketAdapter.publish(CH_SEND, uuid + ": Not Blocks");
        }

        private void returnBlock(String uuid) {
            for (FragmentPRPLab4_2.Block block : listBlocks) {
                if (block.getUuid().equals(uuid)) {
                    block.setUuid(null);
                    break;
                }
            }
        }

        private void addResult(String uuid, String result) {
            for (FragmentPRPLab4_2.Block block : listBlocks) {
                if (block.getUuid().equals(uuid)) {
                    block.setComplete(true);
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

                socketPubSub.unsubscribe();
            }
        }

        private void disconnect(SocketAdapter socketAdapter) {
            try {
                if (socketAdapter != null) {
                    socketAdapter.close();
                }
            } catch (Exception ex) {
                Log.e(TAG, "disconnect: " + ex.getMessage());
            }
        }
    }
}
