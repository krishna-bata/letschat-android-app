package com.example.letschat.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Activities.MainActivity;
import com.example.letschat.R;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class FirebaseNotificationService extends FirebaseMessagingService {
    private FirebaseAuth auth;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();
            if (map.get("type").equals("Chatting")) {
                String title = map.get("sender_name");
                String message = map.get("message");
                String receiverId = map.get("receiverId");
                String receiverImage = map.get("receiverImage");
                String messageId = map.get("messageId");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createOreoNotification(title, message, receiverId, receiverImage, messageId);
                } else {
                    createNormalNotification(title, message, receiverId, receiverImage, messageId);
                }
            }
            if (map.get("type").equals("Friend Request")) {
                String title=map.get("title");
                createRequestNotification(title);
            }
        }
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        //updateToken();
        super.onNewToken(s);
    }

    private void updateToken() {
        auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("device_token");
        userRef.setValue(deviceToken);
    }

    private void createNormalNotification(String title, String message, String receiverId, String receiverImage, String messageId) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1000");
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userName", title);
        intent.putExtra("userId", receiverId);
        intent.putExtra("profileImage", receiverImage);
        intent.putExtra("messageId", messageId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85 - 65), builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message, String receiverId, String receiverImage, String messageId) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChannel channel = new NotificationChannel("1000", "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userName", title);
        intent.putExtra("userId", receiverId);
        intent.putExtra("profileImage", receiverImage);
        intent.putExtra("messageId", messageId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification.Builder(this, "1000")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setSound(uri)
                .setContentIntent(pendingIntent)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .build();
        manager.notify(new Random().nextInt(85 - 65), notification);
    }

    private void createRequestNotification(String title) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1000");
        builder.setContentTitle(title)
                .setContentText("Send Friend Request to you")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userName", title);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85 - 65), builder.build());
    }

}
