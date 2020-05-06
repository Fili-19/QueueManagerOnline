package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeActivity extends AppCompatActivity {

    private String TAG = "HomeActivity";
    private TextView tvLoggedUser = null;
    private Button bttGoMap = null;
    private Button bttGoQueue = null;
    private String loggedUser = null;
    private String eventId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvLoggedUser = findViewById(R.id.tvLoggedUser);
        bttGoMap = findViewById(R.id.bttGoMap);
        bttGoQueue = findViewById(R.id.bttGoQueue);

        Intent i = getIntent();
        loggedUser = i.getStringExtra(getString(R.string.LOGIN_NAME));
        if (loggedUser == null) { // Quando si arriva dalla QueueActivity si ha già un eventId della propria coda
            String[] data = i.getStringArrayExtra(getString(R.string.EVENT_ID));
            eventId = data[0];
            loggedUser = data[1];
        }
        else {   // Quando si arriva dalla MainActivity si verifica se l'utente è già registrato in una coda
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            eventId = null;
            db.collection("events")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    db.collection("events").document(document.getId()).collection("users").document(loggedUser)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        Log.d(TAG, document.getId() + " here => " + document.getData());
                                                        eventId = document.getId();
                                                    }
                                                }
                                            });
                                    }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }



        tvLoggedUser.setText("Welcome " + loggedUser);

        bttGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getString(R.string.MAPS_ACTIVITY));
                i.putExtra(getString(R.string.LOGIN_NAME),new String[] {eventId, loggedUser});
                startActivity(i);
            }
        });

        bttGoQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventId != null) {
                    Intent i = new Intent(getString(R.string.QUEUE_ACTIVITY));
                    i.putExtra(getString(R.string.EVENT_ID), new String[]{eventId, loggedUser});
                    startActivity(i);
                }
                else
                    Toast.makeText(HomeActivity.this, "You are not on a queue!", Toast.LENGTH_LONG).show();
            }
        });

    }
}
