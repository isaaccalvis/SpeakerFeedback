package edu.upc.citm.android.speakerfeedback;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;


public class users_list extends AppCompatActivity {

    private TextView users_list;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        users_list = findViewById(R.id.tv_users_list);
        db.collection("users").whereEqualTo("room", "testroom").addSnapshotListener(this, usersListener);

    }

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null)
            {
                Log.e("SpeakerFeedback", "error al rebre usuaris dins un room", e);
                return;
            }
            String nomUsuari = Integer.toString(documentSnapshots.size());
            // Això és per pillar tots els noms que hi ha a la base de dades dins de room
            for (DocumentSnapshot doc : documentSnapshots)
            {
                nomUsuari += doc.getString("name") + "\n";
            }
            users_list.setText(nomUsuari);
        }
    };
}
