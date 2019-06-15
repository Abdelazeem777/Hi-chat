package com.ahk.sayhi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class stayAwakeService extends Service {
    public stayAwakeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        startService(new Intent(this,firebaseMessagingService.class));
        Intent intent = new Intent(this, ReceiverCall.class);
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
        startService(intent);

        new Thread() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), firebaseMessagingService.class));
                Intent intent = new Intent(getApplicationContext(), ReceiverCall.class);
                intent.putExtra("yourvalue", "torestore");
                sendBroadcast(intent);
                startService(intent);

                AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent2 = new Intent(getApplicationContext(), firebaseMessagingService.class);
                PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, intent2, 0);
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000), pi);
            }
        }.run();
    }
}
