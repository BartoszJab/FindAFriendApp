package com.uwb.findafriendapp.classes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.uwb.findafriendapp.ExpandedEventActivity;
import com.uwb.findafriendapp.R;

public class AlertReceiver extends BroadcastReceiver {

    public static final String TAG = "AlertReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");

        Intent eventIntent = new Intent(context, ExpandedEventActivity.class);
        String eventDate = intent.getStringExtra("eventDate");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, eventIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.clock_icon)
                .setContentTitle("You have incoming events!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(0, builder.build());
    }
}
