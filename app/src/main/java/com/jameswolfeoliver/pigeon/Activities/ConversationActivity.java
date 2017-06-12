package com.jameswolfeoliver.pigeon.Activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.jameswolfeoliver.pigeon.Adapters.MessageAdapter;
import com.jameswolfeoliver.pigeon.Listeners.PaginatedScrollListener;
import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Receivers.IncomingMessageReceiver;
import com.jameswolfeoliver.pigeon.SqlWrappers.MessagesWrapper;
import com.jameswolfeoliver.pigeon.SqlWrappers.SqlCallback;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;

import Models.Contact;
import Models.Conversation;
import Models.Message;

public class ConversationActivity extends AppCompatActivity
        implements View.OnFocusChangeListener, View.OnClickListener {
    public static final String CONVERSATION_EXTRA = "extra_conversation";
    private static final String LOG_TAG = ConversationActivity.class.getSimpleName();
    private static final String SENT_ACTION = "SMS_SENT_ACTION";
    private static final String DELIVERED_ACTION = "SMS_DELIVERED_ACTION";
    private static final int LISTENER_ID = 55;


    private AppCompatEditText chatEditText;
    private RecyclerView messageRecyclerView;
    private AppCompatImageView sendButton;
    private PaginatedScrollListener<Message> paginatedScrollListener;
    private MessagesWrapper messagesWrapper;
    private MessageAdapter messageAdapter;
    private Conversation conversation;
    private Contact contact;
    private SmsReceiver smsReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversation = (getIntent().getExtras() != null) ? (Conversation) getIntent().getSerializableExtra(CONVERSATION_EXTRA) : null;
        if (conversation == null) {
            Toast.makeText(PigeonApplication.getAppContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_conversation);
        contact = ContactCacheManager.getInstance().getContact(conversation.getAddress());

        // Setup views
        this.chatEditText = (AppCompatEditText) findViewById(R.id.action_message);
        this.chatEditText.setOnFocusChangeListener(this);
        this.messageRecyclerView = (RecyclerView) findViewById(R.id.action_view_messages);
        this.sendButton = (AppCompatImageView) findViewById(R.id.action_send);
        this.sendButton.setOnClickListener(this);

        getSupportActionBar().setTitle(contact.getName());
        getSupportActionBar().setHomeButtonEnabled(true);

        // Setup data
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        this.messageRecyclerView.setLayoutManager(linearLayoutManager);
        this.messagesWrapper = new MessagesWrapper(this, Integer.toString(conversation.getThreadId()));
        this.messageAdapter = new MessageAdapter(ConversationActivity.this, new ArrayList<Message>(), contact);
        this.messageRecyclerView.setAdapter(messageAdapter);

        this.paginatedScrollListener = new PaginatedScrollListener<Message>(linearLayoutManager, LISTENER_ID, 5, messagesWrapper) {
            private int lastLoadingItem;

            @Override
            protected void paginated(final ArrayList<Message> messages) {
                messageAdapter.removeLoading(lastLoadingItem);
                if (!messages.isEmpty()) {
                    messageRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            messageAdapter.append(messages);
                            loading.set(false);
                        }
                    });
                }
                Log.i(LOG_TAG, "Paginated");
            }

            @Override
            protected void paginating() {
                lastLoadingItem = messageAdapter.appendLoading();
                messageRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        messageRecyclerView.scrollToPosition(lastLoadingItem);
                    }
                });
                Log.i(LOG_TAG, "Paginating...");
            }
        };
        messageRecyclerView.addOnScrollListener(paginatedScrollListener);
        smsReceiver = new SmsReceiver();
    }

    private void showSoftKeyboard() {
        if (chatEditText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(chatEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.action_message:
                Log.d("James", "Edit text focused? " + Boolean.toString(b));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_send:
                String message = chatEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendTextMessage(message);
                    chatEditText.setText("");
                }
                break;
        }
    }

    private void sendTextMessage(String message) {
        ArrayList<String> messageParts = SmsManager.getDefault().divideMessage(message);

        SmsBroadcastReceiver sentSmsBroadcastReceiver = new SmsBroadcastReceiver(messageParts.size()) {
            MessageInfo messageInfo = new MessageInfo();
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        messageInfo.fail("Error - Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        messageInfo.fail("Error - No Service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        messageInfo.fail("Error - Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        messageInfo.fail("Error - Radio off");
                        break;
                }

                numberOfMessageParts--;
                if (numberOfMessageParts <= 0) {
                    unregisterReceiver(this);
                    if (messageInfo.isFailed()) {
                        Log.d(LOG_TAG, "Send SMS Failure");
                    } else {
                        Log.d(LOG_TAG, "Send SMS Success");
                    }
                }
            }
        };

        SmsBroadcastReceiver deliveredSmsBroadcastReceiver = new SmsBroadcastReceiver(messageParts.size()) {
            @Override
            public void onReceive(Context context, Intent intent) {
                numberOfMessageParts--;
                if (numberOfMessageParts <= 0) {
                    unregisterReceiver(this);
                    getNewMessages();
                    Log.d(LOG_TAG, "SMS Delivered");
                }
            }
        };

        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>(messageParts.size());
        Intent sentIntent = new Intent(SENT_ACTION);

        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>(messageParts.size());
        Intent deliveredIntent = new Intent(DELIVERED_ACTION);

        for (int i = 0; i < messageParts.size(); i++) {
            sentPendingIntents.add(PendingIntent.getBroadcast(this, 0, sentIntent, 0));
            deliveredPendingIntents.add(PendingIntent.getBroadcast(this, 0, deliveredIntent, 0));
        }

        registerReceiver(sentSmsBroadcastReceiver, new IntentFilter(SENT_ACTION));
        registerReceiver(deliveredSmsBroadcastReceiver, new IntentFilter(DELIVERED_ACTION));
        SmsManager.getDefault().sendMultipartTextMessage(Long.toString(conversation.getAddress()),
                null, messageParts, sentPendingIntents, deliveredPendingIntents);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(smsReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationsManager.removeAllNotificationsForConversation(conversation.getThreadId(), this);

        if (messageAdapter.getItemCount() == 0) {
            // Manually do first call
            paginatedScrollListener.forcePaginate();
        } else {
           getNewMessages();
        }

        // Register sms broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    private void getNewMessages() {
        messagesWrapper.find(LISTENER_ID, new SqlCallback<Message>() {
            @Override
            public void onQueryComplete(ArrayList<Message> results) {
                messageAdapter.filteredPrepend(results);
                messageRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        messageRecyclerView.scrollToPosition(0);
                    }
                });
                messagesWrapper.unregisterCallback(LISTENER_ID);
            }
        }, String.valueOf(System.currentTimeMillis()));
    }

    private void onMessageReceived(Message message) {
        messageAdapter.onNewMessage(message);
        messageRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                messageRecyclerView.scrollToPosition(0);
            }
        });
    }

    private class SmsReceiver extends IncomingMessageReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

            if (messageAddress != null && !messageAddress.isEmpty()) {
                Contact.PhoneNumber phoneNumber = new Contact.PhoneNumber(null, messageAddress);
                if (phoneNumber.getNumber() == conversation.getAddress()) {
                    final Message message = new Message.Builder(conversation.getThreadId())
                            .setAddress(phoneNumber.getNumber())
                            .setBody(messageBody)
                            .setDate(messageDate)
                            .setType(Conversation.TYPE_SENDER)
                            .build();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onMessageReceived(message);
                        }
                    });
                    abortBroadcast();
                }
            }
        }
    }

    private abstract class SmsBroadcastReceiver extends BroadcastReceiver {
        protected int numberOfMessageParts;

        private SmsBroadcastReceiver(int numberOfMessageParts) {
            this.numberOfMessageParts = numberOfMessageParts;
        }
    }


    private class MessageInfo {
        String reason;
        boolean failed;

        private void fail(String reason) {
            this.reason = reason;
            failed = true;
        }

        private boolean isFailed() {
            return failed;
        }
    }
}
