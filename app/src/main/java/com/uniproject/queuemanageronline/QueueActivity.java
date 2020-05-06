package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.uniproject.queuemanageronline.utils.CounterUtils;

import java.util.List;

//todo implement onBackPressed()
public class QueueActivity extends AppCompatActivity {

    private String TAG = "QueueActivity";
    private TextView tvCurrentPos = null;
    private Button bttShowQr = null;
    private Button bttCancelQueue = null;
    private Button bttHome = null;
    private String eventId = null;
    private String loggedUser = null;
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
        loggedUser = data[1];

        db.collection("events").document(eventId).collection("users").document(loggedUser)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Query usersBeforeThis = db.collection("events").document(eventId).collection("users")
                                .orderBy("position")
                                .endAt(documentSnapshot);
                        usersBeforeThis.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Log.d(TAG, "here");
                                Log.d(TAG, String.valueOf(queryDocumentSnapshots.getDocuments().size()));
                                int currentPos = queryDocumentSnapshots.getDocuments().size();
                                tvCurrentPos.setText(String.valueOf(currentPos));
                                Log.d(TAG, "here here");
                            }
                        });
                    }
                });

        bttCancelQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("events").document(eventId).collection("users").document(loggedUser)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(QueueActivity.this, "Queue canceled!", Toast.LENGTH_LONG).show();
                            }
                        });
                CounterUtils.decrementCounter(getCounterRef(), 10);
                Intent i = new Intent(getString(R.string.HOME_ACTIVITY));
                i.putExtra(getString(R.string.LOGIN_NAME), loggedUser);
                startActivity(i);
            }
        });

        bttHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getString(R.string.HOME_ACTIVITY));
                i.putExtra(getString(R.string.EVENT_ID), new String[]{eventId, loggedUser});
                startActivity(i);
            }
        });



    }

    private DocumentReference getCounterRef() {
        return db.collection("counters").document(eventId);
    }
}
