package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class UserProfileActivity extends AppCompatActivity {

    private ImageButton backButton, logoutButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getUi();
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        });
        logoutButton.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        });
    }

    public void getUi(){
        backButton = findViewById(R.id.back_button);
        logoutButton = findViewById(R.id.logout_btn);
    }
}