package com.lstu.kovalchuk.androidlabs;

import java.io.*;

import com.neovisionaries.ws.client.*;


public class EchoClient extends Thread{
    private static final int PORT = 3001;
    private static final String SERVER = "ws://192.168.0.2:" + PORT;
    private static final int TIMEOUT = 5000;
    private WebSocket ws;
    private WebSocketAdapter wsa;

    public EchoClient(WebSocketAdapter wsa) throws Exception {
        this.wsa = wsa;
        ws = connect();
    }

    private WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
                .setConnectionTimeout(TIMEOUT)
                .createSocket(SERVER)
                .addListener(wsa)
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
    }

    public WebSocket getWebSocket(){
        return ws;
    }
}
