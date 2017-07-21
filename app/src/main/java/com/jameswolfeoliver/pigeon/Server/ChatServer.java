package com.jameswolfeoliver.pigeon.Server;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Models.ClientRequests.MessageRequest;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Receivers.IncomingMessageReceiver;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ChatServer extends NanoWSD {
    private static final String LOG_TAG = ChatServer.class.getSimpleName();
    public final static int DEFAULT_PORT = 9090;
    private ChatWebSocket chatWebSocket;
    private ExecutorService helperThread;
    private SmsReceiver smsReceiver;

    public ChatServer(int port) {
        super(port);
        helperThread = PigeonApplication.getInstance().getHelperThread();
    }

    @Override
    public void stop() {
        super.stop();
    }

    public boolean isWebsocketOpen() {
        return chatWebSocket != null && chatWebSocket.isOpen();
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        smsReceiver = new SmsReceiver(null);
        chatWebSocket = new ChatWebSocket(handshake);
        return chatWebSocket;
    }

    private void send(final String msg) {
        helperThread.submit(() -> {
            try {
                chatWebSocket.send(msg);
            } catch (IOException e) {
                Log.e(LOG_TAG, "ChatWebSocket failed to send: " + msg, e);
            }
        });
    }

    private class SmsReceiver extends IncomingMessageReceiver {
        Conversation conversation;

        private SmsReceiver(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

            if (messageAddress != null && !messageAddress.isEmpty()) {
                Contact.PhoneNumber phoneNumber = new Contact.PhoneNumber(null, messageAddress);
                if (phoneNumber.getNumber() == conversation.getAddress()
                        || phoneNumber.getNumber() == conversation.getAddress()) {
                    send(messageBody);
                }
            }
        }
    }

    private class ChatWebSocket extends WebSocket {

        public ChatWebSocket(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            // Register sms broadcast receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            PigeonApplication.getAppContext().registerReceiver(smsReceiver, filter);
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {

        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            String msg = message.getTextPayload();
            try {
                message.setUnmasked();
                if (msg.contains("heartbeat")) {
                    sendFrame(message);
                } else {
                    MessageRequest messageRequest = PigeonApplication.getGson().fromJson(msg, MessageRequest.class);
                    smsReceiver.conversation = messageRequest.getConversation();
                    ArrayList<String> messageParts = SmsManager.getDefault().divideMessage(messageRequest.getMessage());
                    SmsManager.getDefault().sendMultipartTextMessage(String.valueOf(messageRequest.getConversation().getAddress()), null, messageParts, null, null);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "ChatWebSocket failed to send: " + msg, e);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {

        }

        @Override
        protected void onException(IOException exception) {
            Log.e(LOG_TAG, "ChatWebSocket encountered exception", exception);
        }
    }
}
