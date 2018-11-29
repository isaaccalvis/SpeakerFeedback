package edu.upc.citm.android.speakerfeedback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class FirestoreListenerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback", "FirestoreListenerService.onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("SpeakerFeedback", "FirestoreListenerService.onStartCommand");
        Intent intent2 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent2, 0);

        // Todo: Crear una notificació i cridar startForeground, perque android no xapi el servei
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format("Connectat a 'testroom'"))
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SpeakerFeedback", "FirestoreListenerService.onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}