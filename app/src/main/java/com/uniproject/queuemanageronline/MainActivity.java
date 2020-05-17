package com.uniproject.queuemanageronline;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private EditText etName = null;
    private Button bttLogin = null;
    private String loggedUser = null;
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(LOCATION_PERMISSION_REQUEST_CODE);

        etName = findViewById(R.id.etName);
        bttLogin = findViewById(R.id.bttLogin);
        db = FirebaseFirestore.getInstance();

        bttLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loggedUser = etName.getText().toString();
                if (!loggedUser.isEmpty()) {
                    // Login logic
                    db.collection("clients")
                            .document(loggedUser)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        // if client already exist check password and exit if not correct
                                        // todo to be implemented
                                    } else {
                                        // if client does not exist create new client on database
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("name", loggedUser);
                                        user.put("password", null);
                                        user.put("myEvent", null);
                                        db.collection("clients")
                                                .document(loggedUser)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error writing document", e);
                                                    }
                                                });
                                    }
                                    // moving to home_activity
                                    Intent i = new Intent(getString(R.string.HOME_ACTIVITY));
                                    i.putExtra(getString(R.string.LOGIN_NAME), loggedUser);
                                    startActivity(i);
                                }
                            });
                }
            }
        });
    }

    public void checkPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, requestCode);
        } else {
            Toast.makeText(MainActivity.this, "Permission already granted",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Location Permission Granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this,
                        "Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                checkPermission(LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
}
