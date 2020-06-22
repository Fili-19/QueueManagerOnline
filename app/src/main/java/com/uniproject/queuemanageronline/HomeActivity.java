package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private String TAG = "HomeActivity";
    private TextView tvLoggedUser = null;
    private Button bttGoMap = null;
    private Button bttGoQueue = null;
    private Button bttLogout = null;
    private String loggedUser = null;
    private String eventId = null;
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvLoggedUser = findViewById(R.id.tvLoggedUser);
        bttGoMap = findViewById(R.id.bttGoMap);
        bttGoQueue = findViewById(R.id.bttGoQueue);
        bttLogout = findViewById(R.id.bttLogout);
        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        loggedUser = i.getStringExtra(getString(R.string.LOGIN_NAME));

        // set welcome message
        String welcomeMessage = "Welcome " + loggedUser;
        tvLoggedUser.setText(welcomeMessage);

        // moving to mapsActivity
        // this activity allows to find event on the map and queue up if allowed
        bttGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMyEventIdAndStartActivity(getString(R.string.MAPS_ACTIVITY));
            }
        });

        // moving to queueActivity
        // this activity shows information about user's queue
        bttGoQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMyEventIdAndStartActivity(getString(R.string.QUEUE_ACTIVITY));
            }
        });

        // logout
        bttLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this , MainActivity.class);
                startActivity(i);
            }
        });

    }

    private void startActivity(String activity) {
        Intent i = new Intent(activity);
        i.putExtra(getString(R.string.LOGIN_EVENT_ID_NAME), new String[]{eventId, loggedUser});
        startActivity(i);
    }

    private void findMyEventIdAndStartActivity(final String activity) {
        // if the user is already on a queue, eventId will be updated otherwise not
        db.collection("clients").document(loggedUser)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                        eventId = (String) documentSnapshot.getData().get("myEvent");
                        if (eventId != null) {
                            db.collection("events").document(eventId).collection("users").document(loggedUser)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (!documentSnapshot.exists()) {
                                                db.collection("clients").document(loggedUser)
                                                        .update("myEvent", null);
                                                eventId = null;
                                            }
                                            startActivity(activity);
                                        }
                                    });
                        }
                        else
                            startActivity(activity);

                    }
                });
    }
}
