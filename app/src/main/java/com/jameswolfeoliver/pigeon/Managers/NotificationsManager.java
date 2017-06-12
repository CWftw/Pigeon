package com.jameswolfeoliver.pigeon.Managers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.jameswolfeoliver.pigeon.Activities.ConnectionActivity;
import com.jameswolfeoliver.pigeon.Activities.ConversationActivity;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import Models.Contact;
import Models.Conversation;
import Models.Message;

public class NotificationsManager {
    private static final String CONVERSATION_TAG = "conversation";
    private static final String REMOTE_CLIENT_TAG = "remote_client";
    private static final int REMOTE_CLIENT_ID = 192837;

    public static void createNotificationForRemoteLogin(String clientName, String clientIp) {
        Context context = PigeonApplication.getAppContext();
        String summary = context.getString(R.string.connection_requested);
        String body = String.format(context.getString(R.string.user_connection_requested), clientName, clientIp);
        Intent confirmConnectionIntent = new Intent(context, ConnectionActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, confirmConnectionIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = getNotificationBuilder(context, context.getResources().getString(R.string.app_name), summary, body)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(REMOTE_CLIENT_TAG, REMOTE_CLIENT_ID, notification);
    }

    public static void createNotificationForMessageReceived(Conversation conversation, Message message, Contact contact) {
        Context context = PigeonApplication.getAppContext();
        String summary = String.format(context.getString(R.string.message_notification_summary), contact.getName(), message.getBody());
        Intent conversationIntent = new Intent(context, ConversationActivity.class);
        conversationIntent.putExtra(ConversationActivity.CONVERSATION_EXTRA, conversation);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, conversationIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = getNotificationBuilder(context, contact.getName(), summary, message.getBody())
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(CONVERSATION_TAG, conversation.getThreadId(), notification);
    }

    public static void removeAllNotificationsForConversation(int threadId, Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CONVERSATION_TAG, threadId);
    }

    public static void removeAllNotificationsForRemoteClient(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(REMOTE_CLIENT_TAG, REMOTE_CLIENT_ID);
    }

    public static void removeAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context, String title,
                                                                     String summary, String body) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.app_icon_circle)
                .setContentTitle(title)
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        return builder;
    }
}
