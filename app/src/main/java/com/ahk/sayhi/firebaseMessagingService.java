package com.ahk.sayhi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class firebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        initChannels(this);

        String notificationTitle=remoteMessage.getData().get("title");
        String notificationMessage=remoteMessage.getData().get("body");
        String clickAction=remoteMessage.getData().get("click_action");
        String fromUserId=remoteMessage.getData().get("fromUserId");
        Log.i("fromUserId",fromUserId);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"default")
                .setSmallIcon(R.drawable.main_logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage+" has sent you a friend request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);



        int notificationId=(int)System.currentTimeMillis();
        Log.i("notificationId",String.valueOf(notificationId));
        // Create an explicit intent for an Activity in your app
        Bundle bundle=new Bundle();
        bundle.putString("userId", fromUserId);
        bundle.putString("userName",notificationMessage);
        bundle.putInt("notificationId",notificationId);
        Intent intent = new Intent(clickAction);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define

        notificationManager.notify(notificationId, mBuilder.build());



    }
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }
}
