package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uniproject.queuemanageronline.utils.CounterUtils;

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
    private ImageView ivQr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        tvCurrentPos = findViewById(R.id.tvCurrentPos);
        bttShowQr = findViewById(R.id.bttShowQr);
        bttCancelQueue = findViewById(R.id.bttCancelQueue);
        bttHome = findViewById(R.id.bttHome);
        ivQr = findViewById(R.id.ivQr);
        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String[] data = i.getStringArrayExtra(getString(R.string.LOGIN_EVENT_ID_NAME));
        if (data != null) {
            eventId = data[0];
            loggedUser = data[1];
        }// exit
        if (eventId != null) {
            // search for the number of users before loggedUser and set the value on the display
            //todo implement listener
            db.collection("events").document(eventId).collection("users").document(loggedUser)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Query usersBeforeThis = db.collection("events").document(eventId).collection("users")
                                        .orderBy("insertTime")
                                        .endAt(documentSnapshot);
                                usersBeforeThis.get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                Log.d(TAG, String.valueOf(queryDocumentSnapshots.getDocuments().size()));
                                                int currentPos = queryDocumentSnapshots.getDocuments().size();
                                                tvCurrentPos.setText(String.valueOf(currentPos));
                                            }
                                        });
                            }
                            else
                                Toast.makeText(QueueActivity.this, "You are not on a queue", Toast.LENGTH_LONG).show();

                        }
                    });

            // remove loggedUser from the queue
            bttCancelQueue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("events").document(eventId).collection("users").document(loggedUser)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(QueueActivity.this, "Queue canceled!", Toast.LENGTH_LONG).show();
                                    db.collection("clients").document(loggedUser)
                                            .update("myEvent", null)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                }
                                            });
                                    CounterUtils.decrementCounter(db.collection("counters").document(eventId), CounterUtils.Shards);
                                }
                            });
                    // moving to homeActivity
                    Intent i = new Intent(getString(R.string.HOME_ACTIVITY));
                    i.putExtra(getString(R.string.LOGIN_NAME), loggedUser);
                    startActivity(i);
                }
            });

            // make and show the qr code
            bttShowQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    try {
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(loggedUser, BarcodeFormat.QR_CODE, 400, 400);
                        ivQr.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            Toast.makeText(QueueActivity.this, "You are not on a queue", Toast.LENGTH_LONG).show();
            bttCancelQueue.setActivated(false);
            bttShowQr.setActivated(false);
        }
        // go back to homeActivity
        bttHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getString(R.string.HOME_ACTIVITY));
                i.putExtra(getString(R.string.LOGIN_NAME), loggedUser);
                startActivity(i);
            }
        });

    }
}
