package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    TextView tvLoggedUser = null;
    Button bttGoMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvLoggedUser = findViewById(R.id.tvLoggedUser);
        bttGoMap = findViewById(R.id.bttGoMap);

        Intent i = getIntent();
        final String loggedUser = i.getStringExtra(getString(R.string.LOGIN_NAME));
        tvLoggedUser.setText("Welcome " + loggedUser);

        bttGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getString(R.string.MAPS_ACTIVITY));
                i.putExtra(getString(R.string.LOGIN_NAME), loggedUser);
                startActivity(i);
            }
        });

    }
}
