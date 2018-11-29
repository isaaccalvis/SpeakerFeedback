package edu.upc.citm.android.speakerfeedback;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.net.Uri;
import android.os.Build;
import android.text.StaticLayout;

public class App extends Application {

    public static final String CHANNEL_ID = "SpeakerFeedback";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // Versió major que Oreo
        {
            NotificationChannel channel = new NotificationChannel(
              CHANNEL_ID,
              "SpeakerFeedback Channel",
              NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
