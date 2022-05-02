package com.nightxstudio.omniconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

public class ConnectWithUs extends AppCompatActivity {

    ImageButton instagramButton;
    ImageButton twitterButton;
    TextView year;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_with_us);

        instagramButton = findViewById(R.id.instagramButton);
        twitterButton = findViewById(R.id.twitterButton);
        year = findViewById(R.id.year);

        instagramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl("https://www.instagram.com/saipritampanda/");
            }
        });

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUrl("https://twitter.com/SaiPritamPanda1");
            }
        });

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        year.setText("© Omni Converter " + currentYear);

    }

    public void goToUrl(String url) {
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}