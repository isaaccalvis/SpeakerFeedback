package edu.upc.citm.android.speakerfeedback;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ChangeRoom extends AppCompatActivity {


    private TextView editText;
    private TextView passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_room);
        editText = findViewById(R.id.changeRoomTextEdit2);
        passwordText = findViewById(R.id.changeRoomPswrdTextEdit);
    }

    public void changeButton(View view) {
        Intent intent = new Intent( this, MainActivity.class);
        intent.putExtra("newRoom", editText.toString());
        intent.putExtra("passwordRoom", passwordText.toString());
        startActivity(intent);
    }
}