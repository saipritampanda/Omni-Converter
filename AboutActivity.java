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

public class AboutActivity extends AppCompatActivity {

    TextView year;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        year = findViewById(R.id.year);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        year.setText("© Omni Converter " + currentYear);

    }

}