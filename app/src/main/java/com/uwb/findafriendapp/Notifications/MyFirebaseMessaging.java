package com.uwb.findafriendapp.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uwb.findafriendapp.ChatActivity;
import com.uwb.findafriendapp.InvitationsActivity;
import com.uwb.findafriendapp.classes.App;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String TAG = "MyFirebaseMessaging";
    private static int ID = 0;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String activityString = remoteMessage.getData().get("activityString");

        Log.d(TAG, "remoteMessage.getData().get tittle: " + remoteMessage.getData().get("title"));
        Log.d(TAG, "remoteMessage.getData().get body: " + remoteMessage.getData().get("body"));
//        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Log.d(TAG, "JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ: " + j);

        Intent intent;
        switch (activityString) {
            case "InvitationsActivity":
                intent = new Intent(this, InvitationsActivity.class);
                ID = 10;
                break;
            case "ChatActivity":
                ID = 15;
                intent = new Intent(this, ChatActivity.class);
                break;
            default:
                intent = new Intent();
                break;

        }

        Bundle bundle = new Bundle();
        bundle.putString("userID", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification1 = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }
        i+=ID;
        Log.d(TAG, "MyFirebaseMessaging i = " + i);
        manager.notify(i, notification1);
    }
}
