package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uniproject.queuemanageronline.utils.CounterUtils;

import java.util.HashMap;
import java.util.Map;
// todo implement onResume method
public class EventActivity extends AppCompatActivity {

    private String TAG = "QueueActivity";
    private TextView tvDescriptionEvent = null;
    private TextView tvCounterEvent = null;
    private Button bttInsertQueue = null;
    private String eventId = null;
    private String loggedUser = null;
    private String loggedUserId = null;
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
        String[] data = i.getStringArrayExtra(getString(R.string.EVENT_ID));
        eventId = data[0];
        loggedUser = data[1];

        DocumentReference docRef = db.collection("events").document(eventId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map event = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + event);
                        // Show event information
                        tvDescriptionEvent.setText((String) event.get("description"));
                        getCounterRef().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        CounterUtils.getCount(document.getReference()).addOnCompleteListener(new OnCompleteListener<Integer>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Integer> task) {
                                                if(task.isSuccessful())
                                                    setCounterText(task.getResult());
                                                else
                                                    Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        });
                                    } else {
                                        CounterUtils.createCounter(getCounterRef(), 10);
                                        setCounterText(0);
                                    }
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");//todo document not found
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        bttInsertQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("events").document(eventId).collection("users")
                .orderBy("position", Query.Direction.DESCENDING)
                .limit(1)// Get the last user on this queue
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long position = 1;
                            for (QueryDocumentSnapshot document : task.getResult()) {// if no users on the queue skip this cycle
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                position = ((long) document.getData().get("position")) + 1;
                            }
                            Map<String, Object> user = new HashMap<>();// Create and add new user on the queue
                            user.put("name", loggedUser);
                            user.put("position", position);
                            db.collection("events").document(eventId).collection("users")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                            loggedUserId = documentReference.getId();
                                            CounterUtils.incrementCounter(getCounterRef(), 10);
                                            //to be completed start
                                            //todo generate and store qr code
                                            //todo find a way to store loggedUserId
                                            Intent i = new Intent(getString(R.string.QUEUE_ACTIVITY));
                                            i.putExtra(getString(R.string.EVENT_ID), new String[] {eventId, loggedUserId});
                                            startActivity(i);
                                            //to be completed end
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {//todo implement showToast feedback
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());//todo implement showToast feedback
                        }
                    }
                });
            }
        });
    }

    private DocumentReference getCounterRef() {
        return db.collection("counters").document(eventId);
    }

    private void setCounterText(int n) {
        tvCounterEvent.setText("Total users on queue : " + n);
    }
}
