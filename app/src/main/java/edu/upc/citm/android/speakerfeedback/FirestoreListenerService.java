package edu.upc.citm.android.speakerfeedback;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class FirestoreListenerService extends Service {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isOpen = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback", "FirestoreListenerService.onCreate");
        db.collection("rooms").document("testroom").collection("polls")
                .whereEqualTo("open", true).addSnapshotListener(pollsListener);
    }

    private EventListener<QuerySnapshot> pollsListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null){
                Log.e("SpeakerFeedback", "Error al rebre la llista de 'polls'");
                return;
            }
            Log.i("SpeakerFeedback", "new poll");
            List<DocumentSnapshot> docs = documentSnapshots.getDocuments();
            if (!docs.isEmpty()) {
                CreateNotification(docs.get(0).getString("question"));
            }
        }
    };

    public void CreateNotification(String msg)
    {
        Intent intent2 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("New Poll:")
                .setContentText(msg)
                .setVibrate(new long[]{100, 200, 100, 200})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("SpeakerFeedback", "FirestoreListenerService.onStartCommand");
        if (isOpen != true) {
            Intent intent2 = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);

            // Todo: Crear una notificació i cridar startForeground, perque android no xapi el servei
            Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                    .setContentTitle(String.format("Connectat a 'testroom'"))
                    .setSmallIcon(R.drawable.ic_message)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            isOpen = true;
        }

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