package com.jameswolfeoliver.pigeon.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

public class IncomingMessageReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = IncomingMessageReceiver.class.getSimpleName();
    private static final String MESSAGE_EXTRA = "pdus";
    private static final String FORMAT_EXTRA = "format";

    protected String messageBody;
    protected long messageDate;
    protected String messageAddress;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras() != null) {
            String format = intent.getStringExtra(FORMAT_EXTRA);
            Object[] rawMessage = (Object[]) intent.getExtras().get(MESSAGE_EXTRA);
            if (rawMessage == null || rawMessage.length == 0) return;

            SmsMessage[] messages = new SmsMessage[rawMessage.length];
            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) rawMessage[i]);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) rawMessage[i], format);
                }
            }

            SmsMessage sms = messages[0];
            if (messages.length == 1 || sms.isReplace()) {
                messageBody = sms.getDisplayMessageBody();
            } else {
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                messageBody = bodyText.toString();
            }

            messageAddress = sms.getDisplayOriginatingAddress();
            messageDate = sms.getTimestampMillis();

            Log.i(LOG_TAG, String.format("Received Sms Message from: %s at %d\n\t%s", messageAddress, messageDate, messageBody));
        }
    }
}
