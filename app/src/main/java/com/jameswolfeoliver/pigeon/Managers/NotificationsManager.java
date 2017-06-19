package com.jameswolfeoliver.pigeon.Managers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import com.jameswolfeoliver.pigeon.Activities.ConnectionActivity;
import com.jameswolfeoliver.pigeon.Activities.ConversationActivity;
import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Models.Message;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Services.MessageReplyService;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

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

    public static void createNotificationForMessageReceived(Context context, Conversation conversation, Message message, Contact contact) {
        String summary = String.format(context.getString(R.string.message_notification_summary), contact.getName(), message.getBody());
        Intent conversationIntent = new Intent(context, ConversationActivity.class);
        conversationIntent.putExtra(ConversationActivity.CONVERSATION_EXTRA, conversation);

        String replyLabel = context.getString(R.string.message_reply);
        RemoteInput remoteInput = new RemoteInput.Builder(MessageReplyService.MESSAGE_TEXT_KEY)
                .setLabel(replyLabel)
                .build();

        Intent replyIntent = new Intent(context, MessageReplyService.class);
        replyIntent.putExtra(MessageReplyService.CONVERSATION_KEY, conversation);
        PendingIntent replyPendingIntent = PendingIntent.getService(context, 0, replyIntent, 0);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_sms,
                context.getString(R.string.message_reply), replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, conversationIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = getNotificationBuilder(context, contact.getName(), summary, message.getBody())
                .setContentIntent(pendingIntent)
                .addAction(replyAction)
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

        builder.setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        return builder;
    }
}
