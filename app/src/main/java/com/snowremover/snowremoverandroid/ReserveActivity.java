package com.snowremover.snowremoverandroid;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

public class ReserveActivity extends AppCompatActivity {

    private TextView reserveDate, reserveTime;
    private AppCompatButton order;
    private ImageButton backButton;
    LocalDate localDate;
    LocalTime localTime;
    String dateString, typeString, totalString, idString, hoursString, quantityString;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        Intent intent = getIntent();
        typeString = intent.getExtras().getString("type");
        dateString = intent.getExtras().getString("date");
        totalString = intent.getExtras().getString("total");
        idString = intent.getExtras().getString("id");
        hoursString = intent.getExtras().getString("hours");
        quantityString = intent.getExtras().getString("quantity");

        backButton = findViewById(R.id.back_button);
        reserveDate = findViewById(R.id.reserve_date);
        reserveTime = findViewById(R.id.reserve_time);
        order = findViewById(R.id.reserve_order);
        reserveDate.setOnClickListener(view -> {
            final Calendar newCalendar = Calendar.getInstance();
            final DatePickerDialog  StartTime = new DatePickerDialog(this, (view12, year, monthOfYear, dayOfMonth) -> {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                localDate = LocalDate.of(year, monthOfYear+1, dayOfMonth);
                reserveDate.setText(dayOfMonth+"/"+(monthOfYear + 1)+"/"+year);
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            StartTime.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            StartTime.show();
        });

        reserveTime.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            localTime = LocalTime.of(hour, minute);
            TimePickerDialog timePickerDialog = new TimePickerDialog(ReserveActivity.this,
                    (view1, hourOfDay, minute1) -> reserveTime.setText(hourOfDay + ":" + minute1), hour, minute, false);
            timePickerDialog.show();
        });

        order.setOnClickListener(view -> {
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
            Date date = Date.from(instant);
            Timestamp timestamp = new Timestamp(date);

            Intent orderIntent = new Intent(getApplicationContext(), ConfrimOrderActivity.class);
            orderIntent.putExtra("type", typeString);
            orderIntent.putExtra("date", timestamp.toDate().toString());
            orderIntent.putExtra("total", totalString);
            orderIntent.putExtra("id", idString);
            orderIntent.putExtra("hours", hoursString);
            orderIntent.putExtra("quantity", quantityString);
            startActivity(orderIntent);

        });

        backButton.setOnClickListener(view -> onBackPressed());
    }
}