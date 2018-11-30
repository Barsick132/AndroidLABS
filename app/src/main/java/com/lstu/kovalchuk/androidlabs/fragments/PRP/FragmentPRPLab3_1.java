package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lstu.kovalchuk.androidlabs.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import redis.clients.jedis.Jedis;

public class FragmentPRPLab3_1 extends Fragment {

    private static final String TAG = "FragmentPRPLab3";

    private static int BLOCK_SIZE;
    private static int COUNT_BLOCKS;

    public FragmentPRPLab3_1() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab3_1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Thread thread = new Thread(() -> {
            RedisAdapter ra = new RedisAdapter();
            String block = ra.getBlock();
            if (block != null) {
                Thread thread1 = new Thread(() -> {
                    List<Word> wordList = calcWordsFromStr(block);
                    /*

                    Подумать над тем, в каком виде это загружать
                    Может объединять в группы слова, которые, встретились одинаковое кол-во раз,
                    например, [{"count" : 1, "strings" : ["я", "аня", "вертолет"]},
                                {"count" : 2, "strings" : ["не", "-", "но"]}]


                    Запускать выполнение подсчетов только по нажатию кнопки.

                     */
                });
                thread1.start();
            }
        });
        thread.start();
    }

    private List<Word> calcWordsFromStr(String block) {
        List<Word> listWord = new ArrayList<>();
        String[] split = block.split("\\s+");
        for (String str : split) {
            if (searchWord(listWord, str) == null) {
                listWord.add(0, new Word(str, 1));
            } else {
                Word w = searchWord(listWord, str);
                listWord.remove(w);
                w.setCount(w.getCount()+1);
                listWord.add(searchPosition(listWord, w.getCount()), w);
            }
        }
        return listWord;
    }

    private Word searchWord(List<Word> wordList, String word){
        for(Word w : wordList){
            if(w.getWord().equals(word)){
                return w;
            }
        }
        return null;
    }

    private int searchPosition(List<Word> wordList, int count){
        int step = wordList.size()/2;
        int pos = step;
        if(count>=wordList.get(wordList.size()-1).getCount()) return wordList.size();
        while (true){
            if(wordList.get(pos).getCount()>count){
                if(step!= 1) step = step/2;
                pos -= step;
            }
            if (wordList.get(pos).getCount()<count){
                if(step!= 1) step = step/2;
                pos += step;
            }
            if(wordList.get(pos).getCount()<count && wordList.get(pos+1).getCount()>count){
                return pos+1;
            }
            if(wordList.get(pos-1).getCount()<count && wordList.get(pos).getCount()>count){
                return pos;
            }
            if(wordList.get(pos).getCount()==count) return pos;
        }
    }

    interface intRedisAdapter {
        Jedis jedis = new Jedis("192.168.0.2");

        String getBlock();
    }

    class RedisAdapter implements intRedisAdapter {
        @Override
        public String getBlock() {
            List<String> res = new ArrayList<>();
            BLOCK_SIZE = Integer.parseInt(jedis.get("block_size"));
            COUNT_BLOCKS = Integer.parseInt(jedis.get("count_blocks"));
            if (COUNT_BLOCKS == 0) return null;
            for (int i = 0; i < COUNT_BLOCKS; i++) {
                boolean taken = Boolean.parseBoolean(jedis.get("flag" + i));
                if (!taken) {
                    jedis.set("flag" + i, "true");
                    return jedis.get("block" + i);
                }
            }
            return null;
        }
    }

    private class Word{
        private String word;
        private int count;

        public Word(String word, int count) {
            this.word = word;
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
