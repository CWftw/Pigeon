package com.jameswolfeoliver.pigeon.Receivers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.jameswolfeoliver.pigeon.Managers.ContactCacheManager;
import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Models.Message;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import com.jameswolfeoliver.pigeon.Utilities.Utils;

public class BackgroundSmsReceiver extends IncomingMessageReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (PigeonApplication.isDefaultSmsApp()
                && !Utils.isStringNullOrEmpty(messageAddress)
                && !Utils.isStringNullOrEmpty(messageBody)
                && messageDate != 0) {
            Contact.PhoneNumber phoneNumber = new Contact.PhoneNumber(null, messageAddress);

            final int THREAD_ID = getThreadId(context, phoneNumber.getNumber());

            final Conversation conversation = new Conversation.Builder(THREAD_ID)
                    .setSnippet(messageBody)
                    .setDate(messageDate)
                    .setAddress(phoneNumber.getNumber())
                    .setType(Conversation.TYPE_SENDER)
                    .build();

            final Message message = new Message.Builder(THREAD_ID)
                    .setAddress(phoneNumber.getNumber())
                    .setBody(messageBody)
                    .setDate(messageDate)
                    .setType(Conversation.TYPE_SENDER)
                    .build();

            final Contact contact = ContactCacheManager.getInstance().getContact(phoneNumber.getNumber());

            NotificationsManager.createNotificationForMessageReceived(context, conversation, message, contact);
        }
    }

    private int getThreadId(Context context, long address) {
        ContentResolver cr = context.getContentResolver();
        Cursor pCur = cr.query(
                Uri.parse("content://mms-sms/canonical-addresses"), new String[]{"_id"},
                "address" + " = ?",
                new String[]{Long.toString(address)}, null);

        String threadId = null;

        if (pCur != null) {
            if (pCur.getCount() != 0) {
                pCur.moveToNext();
                threadId = pCur.getString(pCur.getColumnIndex("_id"));
                Log.d("JAmes", threadId);
            }
            pCur.close();
        }

        return (threadId != null && !threadId.isEmpty()) ? Integer.parseInt(threadId) : Conversation.THREAD_ID_NONE;
    }
}
