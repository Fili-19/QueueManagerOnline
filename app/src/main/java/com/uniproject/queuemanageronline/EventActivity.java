package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uniproject.queuemanageronline.utils.CounterUtils;

import java.util.HashMap;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    private String TAG = "EventActivity";
    private TextView tvDescriptionEvent = null;
    private TextView tvCounterEvent = null;
    private Button bttInsertQueue = null;
    private String eventId = null;
    private String loggedUser = null;
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        tvDescriptionEvent = findViewById(R.id.tvDescriptionEvent);
        tvCounterEvent = findViewById(R.id.tvCounterEvent);
        bttInsertQueue = findViewById(R.id.bttInsertQueue);
        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String[] data = i.getStringArrayExtra(getString(R.string.LOGIN_EVENT_ID_NAME));
        if (data != null) {
            eventId = data[0];
            loggedUser = data[1];
        }// exit

        // search for the event on the database
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map event = documentSnapshot.getData();
                            if (event != null) {
                                Log.d(TAG, "DocumentSnapshot data: " + event);
                                // Show event information
                                tvDescriptionEvent.setText((String) event.get("description"));
                            }
                            // search the counter that references this event and set the value on the display
                            displayEventCounter();
                        }
                        else
                            Log.d(TAG, "No such document: " + eventId);
                    }
                });

        // insert the user in the queue of this event
        bttInsertQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and add new user on the queue
                Map<String, Object> user = new HashMap<>();
                user.put("name", loggedUser);
                user.put("insertTime", FieldValue.serverTimestamp());
                db.collection("events").document(eventId).collection("users").document(loggedUser)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("clients").document(loggedUser)
                                    .update("myEvent", eventId)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                        }
                                    });
                            CounterUtils.incrementCounter(getCounterReference(), CounterUtils.Shards);
                            // moving to queueActivity, this activity shows information about user's queue
                            Intent i = new Intent(getString(R.string.QUEUE_ACTIVITY));
                            i.putExtra(getString(R.string.LOGIN_EVENT_ID_NAME), new String[]{eventId, loggedUser});
                            startActivity(i);
                        }
                    });
            }
        });
    }

    private void setCounterText(int n) {
        String totalUsersOnQueue = "Total users on queue are : " + n;
        tvCounterEvent.setText(totalUsersOnQueue);
    }

    private DocumentReference getCounterReference() {
        return db.collection("counters").document(eventId);
    }

    private void displayEventCounter() {
        getCounterReference()
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // if the counter already exists get and set the value otherwise create and set to the initial value 0
                            CounterUtils.getCount(documentSnapshot.getReference())
                                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                        @Override
                                        public void onSuccess(Integer integer) {
                                            setCounterText(integer);
                                        }
                                    });
                        }
                        else {
                            CounterUtils.createCounter(getCounterReference(), CounterUtils.Shards);
                            setCounterText(0);
                        }
                    }
                });
    }
}