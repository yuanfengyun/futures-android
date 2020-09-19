package com.shinnytech.futures.websocket;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.reqbean.ReqPeekMessageEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.LogUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;

public class WebSocketBase extends WebSocketAdapter {
    protected DataManager sDataManager = DataManager.getInstance();
    protected WebSocket mWebSocketClient;
    protected List<String> mUrls;
    protected int mIndex;
    private int mPongCount;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private long mConnectTime = 0;

    public WebSocketBase(List<String> urls, int index) {
        mIndex = index;
        mUrls = urls;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        LogUtils.e("onConnected "+mUrls.get(0), true);
        mPongCount = 0;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (BaseApplication.issBackGround()) return;

                if (mPongCount > 3) {
                    reConnect();
                    return;
                }
                mWebSocketClient.sendPing();
                mPongCount++;
            }
        };
        mTimer.schedule(mTimerTask, 0, 10000);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        LogUtils.e("onDisconnected "+mUrls.get(0), true);
        reConnect();
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        LogUtils.e("onConnectError", true);
        reConnect();
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onPongFrame(websocket, frame);
        mPongCount--;
    }

    public void reConnect() {
        final long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - mConnectTime <= 10)
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    connect(currentTime);
                }
            }, 10000);
        else{
            connect(currentTime);
        }
    }

    private void connect(long currentTime) {
        mConnectTime = currentTime;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        try {
            if (mWebSocketClient != null) mWebSocketClient.clearListeners();
            mWebSocketClient = new WebSocketFactory()
                    .setVerifyHostname(TRANSACTION_URL.equals(mUrls.get(0)))
                    .setConnectionTimeout(5000)
                    .createSocket(mUrls.get(mIndex))
                    .setMissingCloseFrameAllowed(false)
                    .addListener(this)
                    .addHeader("User-Agent", "tqsdk-python 1.8.3")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJobi1MZ3ZwbWlFTTJHZHAtRmlScjV5MUF5MnZrQmpLSFFyQVlnQ0UwR1JjIn0.eyJqdGkiOiIwY2UwOTM2Ny0xYjk2LTQ0NTktOGU2My1hYWM1ZTA3Mjc1ZTIiLCJleHAiOjE2MTU1Mzk1MTQsIm5iZiI6MCwiaWF0IjoxNTg0MDAzNTE0LCJpc3MiOiJodHRwczovL2F1dGguc2hpbm55dGVjaC5jb20vYXV0aC9yZWFsbXMvc2hpbm55dGVjaCIsInN1YiI6IjYzMzJhZmUwLWU5OWQtNDc1OC04MjIzLWY5OTBiN2RmOGY4NSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNoaW5ueV90cSIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjUzYTEyYmNkLTc3M2EtNDcyZC1iZWVlLWZlMmQ1ODAzYjU0YyIsImFjciI6IjEiLCJzY29wZSI6ImF0dHJpYnV0ZXMiLCJncmFudHMiOnsiZmVhdHVyZXMiOlsiY21iIiwiYWR2Il0sImFjY291bnRzIjpbIioiXX19.BmqzmorwITPd2YLP9EbhlIxkTDNTAY-PNPfM9LwOkOc5XJlSK34nHZwW14mmIScYiohhN5iaVtPrPNsohFfPcH-FxhFmmr9M_xIJLDf4zw2ObcZwVGTQFnIExjdpj2ej82bPT0yoBBFoOH3NhFuK0agifE0WOp0lXf2kzQsQncZ-y9djCEuwbuZapNmdVhGsWWGt7gMd9ZJNrmViZifSWkOrpiowIQ4fOPp1L2DJju8QldwHtyPnYTZtN56x14Xd7v-4-VB3vWEoHB99r36bjhlXJsxuiZrQom0esahgtV_7gx_G95bN04XevriRXG9JzOSoHhpYQFKqjZlSQ4L7vw")
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIndex += 1;
        if (mIndex == mUrls.size()) mIndex = 0;
        mWebSocketClient.connectAsynchronously();
    }

    public void backToForegroundCheck() {
        mConnectTime = 0;
        if (mWebSocketClient == null || !mWebSocketClient.isOpen()) reConnect();
        else sendPeekMessage();
    }

    public void sendPeekMessage(){
        ReqPeekMessageEntity reqPeekMessageEntity = new ReqPeekMessageEntity();
        reqPeekMessageEntity.setAid("peek_message");
        String peekMessage = JSON.toJSONString(reqPeekMessageEntity);
        mWebSocketClient.sendText(peekMessage);
        LogUtils.e(peekMessage, false);
    }

    public String getWebSocketStatus(){
        if (mWebSocketClient == null)return "NULL";
        else return mWebSocketClient.getState().name();
    }

}
