package com.jameswolfeoliver.pigeon.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Models.MessageInfo;
import com.jameswolfeoliver.pigeon.Receivers.SmsBroadcastReceiver;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;

public class MessageReplyService extends Service {
    public static final String LOG_TAG = MessageReplyService.class.getSimpleName();
    public static final String CONVERSATION_KEY = "conversation_key";
    public static final String MESSAGE_TEXT_KEY = "message_text_key";
    private static final String SENT_ACTION = "SMS_SENT_ACTION";
    private static final String DELIVERED_ACTION = "SMS_DELIVERED_ACTION";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle results = RemoteInput.getResultsFromIntent(intent);
        String message = results != null ? results.getCharSequence(MESSAGE_TEXT_KEY).toString() : null;
        Conversation conversation = (intent.getExtras() != null) ? (Conversation) intent.getSerializableExtra(CONVERSATION_KEY) : null;
        if (conversation == null || message == null) {
            Toast.makeText(PigeonApplication.getAppContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        sendTextMessage(conversation, message);
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendTextMessage(final Conversation conversation, String message) {
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
                    NotificationsManager.removeAllNotificationsForConversation(conversation.getThreadId(), context);
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
}
