package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        //eventId = SharedPreferencesUtils.getString(getBaseContext(), getString(R.string.saved_event_code));
        // if the user is already on a queue, eventId will be updated otherwise not
        db.collection("events")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("events").document(document.getId()).collection("users").document(loggedUser)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists())
                                                eventId = document.getId();
                                        }
                                    });
                            // when eventId is found we can stop searching
                            if (eventId != null)
                                break;
                        }
                    }
                });

        // set welcome message
        String welcomeMessage = "Welcome " + loggedUser;
        tvLoggedUser.setText(welcomeMessage);

        // moving to mapsActivity, if eventId is not null we cannot queue up
        // this activity allows to find event on the map and queue up if allowed
        bttGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getString(R.string.MAPS_ACTIVITY));
                i.putExtra(getString(R.string.LOGIN_EVENT_ID_NAME),new String[] {eventId, loggedUser});
                startActivity(i);
            }
        });

        // moving to queueActivity, eventId must be not null that means the user is on a queue
        // this activity shows information about user's queue
        bttGoQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventId != null) {
                    Intent i = new Intent(getString(R.string.QUEUE_ACTIVITY));
                    i.putExtra(getString(R.string.LOGIN_EVENT_ID_NAME), new String[]{eventId, loggedUser});
                    startActivity(i);
                }
                else
                    Toast.makeText(HomeActivity.this, "You are not on a queue!", Toast.LENGTH_LONG).show();
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
}
