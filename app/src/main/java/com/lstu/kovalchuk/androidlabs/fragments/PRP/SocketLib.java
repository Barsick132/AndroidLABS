package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.util.Log;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.HashSet;

class SocketLib {

    private static final int PORT = 3000;
    private static final String TAG = "SocketLib";
    private static final int TIMEOUT = 8000;

    static class SocketAdapter {

        private final WebSocket ws;
        private final Gson gson;

        SocketAdapter(String HOST) throws IOException, WebSocketException {
            gson = new Gson();
            ws = connect(HOST);
        }

        // Создание сокет-подключения
        private WebSocket connect(String host) throws IOException, WebSocketException {
            final String SERVER = "ws://" + host + ":" + String.valueOf(PORT);
            return new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(SERVER)
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect();
        }

        // Подписка на канал
        void subscribe(SocketPubSub socketPubSub, String channel) {
            socketPubSub.setSocket(ws);

            ws.addListener(new WebSocketAdapter() {
                public void onTextMessage(WebSocket websocket, String message) {
                    Log.d(TAG, "onTextMessage: получено сообщение" + message);

                    Message mes = gson.fromJson(message, Message.class);

                    if (mes.type.equals("sub")) { // Подтверждение ПОДписки
                        socketPubSub.onSubscribe(mes.channel);
                        return;
                    }
                    if (mes.type.equals("unsub")) { // Подтверждение ОТписки
                        socketPubSub.onUnsubscribe(mes.channel);
                        return;
                    }
                    if (mes.type.equals("mes")) { // Сообщение канала
                        for (String ch : socketPubSub.channels) {
                            if (ch.equals(mes.channel)) {
                                socketPubSub.onMessage(mes.channel, mes.message);
                            }
                        }
                    }
                }
            });
            ws.sendText(gson.toJson(new Message("sub",channel, null)));  // Подписываемся на канал
        }

        // Публикация в канал
        void publish(String channel, String message){
            ws.sendText(gson.toJson(new Message("mes",channel, message))); // Публикация сообщения в канал
        }

        boolean isConnected(){
            try {
                return ws.isOpen();
            }catch (NullPointerException ex){
                return false;
            }
        }

        // Закрываем сокет
        void close() {
            try {
                ws.sendClose();
                ws.disconnect();
            }catch (Exception ex){
                Log.e(TAG, "close: " + ex.getMessage());
            }
        }
    }

    // Интерфейс обработки ответов сокет-сервера
    private interface interfaceSocketPubSub {
        void onMessage(String channel, String message);

        void onUnsubscribe(String channel);

        void onSubscribe(String channel);
    }

    // Обработчик ответов сокет-сервера
    public static class SocketPubSub implements interfaceSocketPubSub {
        HashSet<String> channels;
        private WebSocket ws;

        SocketPubSub() {
            ws = null;
            channels = new HashSet<>();
        }

        // Добавлеение нового канала влокальный реестр
        void setSocket(WebSocket ws) {
            if (this.ws == null) this.ws = ws;
        }

        // Обработчик подписки
        @Override
        public void onSubscribe(String channel) {
            Log.d(TAG, "onSubscribe: started");
            channels.add(channel);
        }

        // Обработчик входящих сообщений
        @Override
        public void onMessage(String channel, String message) {
            Log.d(TAG, "onMessage: started");
        }

        // Обработчик отписки
        @Override
        public void onUnsubscribe(String channel) {
            Log.d(TAG, "onUnsubscribe: started");
            channels.remove(channel);
        }

        // Функция отписки от всех каналов
        void unsubscribe() {
            Gson gson = new Gson();
            if (ws != null) {
                for (String ch : channels) {
                    ws.sendText(gson.toJson(new Message("unsub",ch, null))); // Отписка от всех каналов
                }
            }
        }
    }

    // Класс сообщения
    private static class Message {
        private String type;
        private String channel;
        private String message;

        Message(String type, String channel, String message) {
            this.type = type;
            this.channel = channel;
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
