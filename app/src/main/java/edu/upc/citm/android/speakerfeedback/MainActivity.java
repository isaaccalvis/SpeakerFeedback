package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REGISTER_USER = 0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId;
    private List<Poll> polls = new ArrayList<>();

    private ListenerRegistration roomRegistration;
    private ListenerRegistration usersRegistration;

    private RecyclerView polls_view;
    private Adapter adapter;
    private TextView users_conected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        users_conected = findViewById(R.id.users_conected);
        adapter = new Adapter();
        polls_view = findViewById(R.id.recycler_view);
        polls_view.setLayoutManager(new LinearLayoutManager(this));
        polls_view.setAdapter(adapter);

        // Busquem a les preferències de l'app l'ID de l'usuari per saber si ja s'havia registrat
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            // Hem de registrar l'usuari, demanem el nom
            Intent intent = new Intent(this, RegisterUserActivity.class);
            startActivityForResult(intent, REGISTER_USER);
            Toast.makeText(this, "Encara t'has de registrar", Toast.LENGTH_SHORT).show();
        } else {
            // Ja està registrat, mostrem el id al Log
            Log.i("SpeakerFeedback", "userId = " + userId);
            enterRoom();
        }
        StartFirestoreService();
    }

    private void StartFirestoreService(){
        Intent intent = new Intent(this, FirestoreListenerService.class);
        intent.putExtra("room", "testroom");
        startService(intent);
    }

    private void StopFirestoreService(){
        Intent intent = new Intent(this, FirestoreListenerService.class);
        stopService(intent);
    }

    private void enterRoom() {
        db.collection("users").document(userId).update("room", "testroom");
    }

    private EventListener<DocumentSnapshot> roomListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
            if (e != null)
            {
                Log.e("SpeakerFeedback", "error al rebre rooms/testroom", e);
                return;
            }
            String name = documentSnapshot.getString("name");
            setTitle(name);
        }
    };

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null)
            {
                Log.e("SpeakerFeedback", "error al rebre usuaris dins un room", e);
                return;
            }
            users_conected.setText(String.format("Num. Users : %d", documentSnapshots.size()));
            //String nomUsuari = Integer.toString(documentSnapshots.size());
            // Això és per pillar tots els noms que hi ha a la base de dades dins de room
            //for (DocumentSnapshot doc : documentSnapshots)
            //{
            //    nomUsuari += doc.getString("name") + "\n";
            //}
            //users_conected.setText(nomUsuari);
        }
    };

    private EventListener<QuerySnapshot> pollsListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null){
                Log.e("SpeakerFeedBack", "Error al rebre la llista de 'polls'");
                return;
            }
            polls = new ArrayList<>();
            for (DocumentSnapshot doc : documentSnapshots){
                Poll poll = doc.toObject(Poll.class);
                poll.setId(doc.getId());
                polls.add(poll);
            }
            Log.i("SpeakerFeedback", String.format("He carregat %d polls.", polls.size()));
            adapter.notifyDataSetChanged();
        }
    };

    protected void onStart()
    {
        super.onStart();
        // Posem un listener al room de la base de dades
        db.collection("rooms").document("testroom").addSnapshotListener(this, roomListener);
        db.collection("users").whereEqualTo("room", "testroom").addSnapshotListener(this, usersListener);
        db.collection("rooms").document("testroom").collection("polls").addSnapshotListener(this, pollsListener);
        /////////////////////////
        db.collection("room").document("testroom").update("room", "testroom", "last_active", new Date());
    }

    //////////////////////
    protected void onDestroy()
    {
        db.collection("users").document("testroom").update("room", FieldValue.delete());
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_USER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    registerUser(name);
                } else {
                    Toast.makeText(this, "Has de registrar un nom", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerUser(String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        db.collection("users").add(fields).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                 Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // textview.setText(documentReference.getId());
                userId = documentReference.getId();
                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                prefs.edit().putString("userId", userId).commit();
                enterRoom();
                Log.i("SpeakerFeedback", "New user: userId = " + userId);
                ////////////////////////////
                db.collection("room").document("testroom").update("room", "testroom", "lasta_ctive", Calendar.getInstance().getTime());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Error creant objecte", e);
                Toast.makeText(MainActivity.this,
                        "No s'ha pogut registrar l'usuari, intenta-ho més tard", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void clickUsersNum(View view) {
        Intent intent = new Intent(this, users_list.class);
        startActivity(intent);
    }

    public void OnPollClick(final int pos) {
        if (polls.get(pos).isOpen()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //if (polls.get(pos).getResults() != null) {
            final String[] options = new String[polls.get(pos).getOptions().size()];
            for (int i = 0; i < polls.get(pos).getOptions().size(); i++) {
                options[i] = polls.get(pos).getOptions().get(i);
            }

            builder
                    .setTitle(polls.get(pos).getQuestion())
                    .setItems(options,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("pollid", polls.get(pos).getId());
                                    map.put("option", which);
                                    //List<Integer> tmpList = polls.get(pos).getResults();
                                    //tmpList.set(which, tmpList.get(which) + 1);
                                    //polls.get(pos).setResults(tmpList);
                                    db.collection("rooms").document("testroom").collection("votes").document(userId).set(map);
                                }
                            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                        }
                    });

            builder.create().show();
            //}
        }
    }

    class ViewHolder extends  RecyclerView.ViewHolder{
        private CardView card_view;
        private TextView label_view;
        private TextView questions_view;
        private TextView options_view;
        private TextView results_view;

        public ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            label_view = itemView.findViewById(R.id.label_view);
            questions_view = itemView.findViewById(R.id.questions_view);
            options_view = itemView.findViewById(R.id.options_view);
            results_view = itemView.findViewById(R.id.results_view);
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    OnPollClick(pos);
                }
            });
        }
    }

    class Adapter extends  RecyclerView.Adapter<ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.poll_view, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Poll poll = polls.get(position);
            if (position == 0)
            {
                holder.label_view.setVisibility(View.VISIBLE);
                if (poll.isOpen())
                {
                    holder.label_view.setText("Active");
                }
                else
                {
                    holder.label_view.setText("Previous");
                }
            }
            else
            {
                if (!poll.isOpen() && polls.get(position - 1).isOpen())
                {
                    holder.label_view.setVisibility(View.VISIBLE);
                    holder.label_view.setText("Previous");
                }
                else
                {
                    holder.label_view.setVisibility(View.GONE);
                }
            }
            holder.card_view.setCardElevation(poll.isOpen() ? 10.0f : 0.0f);
            if (!poll.isOpen()){
                holder.card_view.setBackgroundColor(0xFFF0F0F0);
            }
            holder.questions_view.setText(poll.getQuestion());
            holder.options_view.setText(poll.getOptionsAsString());
            if (poll.getResults() != null) {
                holder.results_view.setText(poll.getResultsAsString());
            }
        }

        @Override
        public int getItemCount() {
            return polls.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu ,menu );
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close_sesion:
                Toast.makeText(MainActivity.this, "Close App", Toast.LENGTH_SHORT).show();
                stopService(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return true;
    }

}