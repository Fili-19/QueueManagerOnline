package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class QueueActivity extends AppCompatActivity {

    private TextView tvCurrentPos = null;
    private Button bttShowQr = null;
    private Button bttCancelQueue = null;
    private Button bttHome = null;
    private String eventId = null;
    private String loggedUserId = null;
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        tvCurrentPos = findViewById(R.id.tvCurrentPos);
        bttShowQr = findViewById(R.id.bttShowQr);
        bttCancelQueue = findViewById(R.id.bttCancelQueue);
        bttHome = findViewById(R.id.bttHome);
        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String[] data = i.getStringArrayExtra(getString(R.string.EVENT_ID));
        eventId = data[0];
        loggedUserId = data[1];

        int currentPos = 0;

        /*db.collection("events").document(eventId).collection("users").document(loggedUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Query usersBeforeThis = db.collection("events").document(eventId).collection("users")
                                .orderBy("position")
                                .endAt(documentSnapshot);
                        usersBeforeThis.get()
                    }
                });*/
    }
}
