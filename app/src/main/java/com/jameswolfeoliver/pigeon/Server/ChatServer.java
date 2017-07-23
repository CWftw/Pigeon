package com.jameswolfeoliver.pigeon.Server;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Models.ClientRequests.MessageRequest;
import com.jameswolfeoliver.pigeon.Models.ClientResponses.Message;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Receivers.IncomingMessageReceiver;
import com.jameswolfeoliver.pigeon.SqlWrappers.ConversationWrapper;
import com.jameswolfeoliver.pigeon.SqlWrappers.ThreadWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;

public class ChatServer extends NanoWSD {
    private static final String LOG_TAG = ChatServer.class.getSimpleName();
    public final static int DEFAULT_PORT = 9090;
    private final static String HEARTBEAT = "--heartbeat--";

    private ChatWebSocket chatWebSocket;
    private ExecutorService helperThread;
    private SmsReceiver smsReceiver;
    private ConversationWrapper conversationWrapper;
    private ThreadWrapper threadWrapper;

    public ChatServer(int port) {
        super(port);
        helperThread = PigeonApplication.getInstance().getHelperThread();
        conversationWrapper = new ConversationWrapper();
        smsReceiver = new SmsReceiver();
        threadWrapper = new ThreadWrapper();
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
        chatWebSocket = new ChatWebSocket(handshake);
        return chatWebSocket;
    }

    private void send(final String body, final String address, final long messageDate) {
        helperThread.submit(() -> {
            try {
                Contact contact = ContactCacheManager.getInstance().updateNow().getContact(Long.parseLong(address));
                Message message = new Message();
                message.setAddress(Long.parseLong(address));
                message.setContact(contact);
                message.setBody(body);
                message.setDate(messageDate);

                int threadId = -1;
                Conversation conversation = null;

                try {
                    Iterable<Integer> threadIterable = threadWrapper.find(() -> address).blockingIterable();
                    Iterator<Integer> threadIds = threadIterable.iterator();
                    while (threadIds.hasNext()) threadId = threadIds.next();

                    final int finalThreadId = threadId;
                    if (finalThreadId == -1) throw new NoSuchElementException("threadId not found");

                    Iterable<Conversation> conversationIterable = conversationWrapper.find(() -> conversationWrapper.selectByThread(finalThreadId)).blockingIterable();
                    Iterator<Conversation> conversations = conversationIterable.iterator();
                    while (conversations.hasNext()) conversation = conversations.next();

                    if (conversation == null) throw new NoSuchElementException("conversation for threadId not found");
                    message.setConversation(conversation);
                } catch (NoSuchElementException e) {
                    Log.e(LOG_TAG, "Couldn't find conversation for address: " + address, e);
                }

                synchronized (chatWebSocket) {
                    chatWebSocket.send(PigeonApplication.getGson().toJson(message));
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "ChatWebSocket failed to send: " + body, e);
            }
        });
    }

    private class SmsReceiver extends IncomingMessageReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (messageAddress != null && !messageAddress.isEmpty()
                    && messageBody != null && !messageBody.isEmpty()) {
                send(messageBody, messageAddress, messageDate);
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
            PigeonApplication.getAppContext().unregisterReceiver(smsReceiver);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            String msg = message.getTextPayload();
            try {
                message.setUnmasked();
                if (msg.equals(HEARTBEAT)) {
                    sendFrame(message);
                } else {
                    MessageRequest messageRequest = PigeonApplication.getGson().fromJson(msg, MessageRequest.class);
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
