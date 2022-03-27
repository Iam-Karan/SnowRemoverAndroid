package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.admin.AdminMainActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseUser mFirebaseUser;
    private String uId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(1*3000);

                    if(mFirebaseUser != null){
                        firestore = FirebaseFirestore.getInstance();
                        uId = mFirebaseUser.getUid();
                        firestore.collection("users").document(uId).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                if (document.exists()) {
                                    String type =  Objects.requireNonNull(document.getData().get("type")).toString();
                                    if(type.equals("Admin")){
                                        Toast.makeText(getApplicationContext(), "Welcome Admin!", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getBaseContext(), AdminMainActivity.class);
                                        startActivity(intent);
                                    }else{
                                        Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                                        startActivity(intent);
                                    }

                                } else {
                                    Log.d("document Not Found", "No such document");
                                }
                            } else {
                                Log.d("error", "get failed with ", task.getException());
                            }
                        }).addOnFailureListener(e -> Log.d("error", e.toString()));

                    }else {
                        Intent i=new Intent(getBaseContext(),HomeScreen.class);
                        startActivity(i);
                    }
                    finish();
                } catch (Exception e) {
                }
            }
        };
        background.start();
    }
}