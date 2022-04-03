package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.admin.AdminMainActivity;

public class MainActivity extends AppCompatActivity {

    private String type = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent;
            if(type.equals("Admin")){
                intent = new Intent(getBaseContext(), AdminMainActivity.class);
            }else{
                intent = new Intent(getBaseContext(), HomeScreen.class);
            }
            startActivity(intent);
        }, 3500);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        if(mFirebaseUser != null){
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String uId = mFirebaseUser.getUid();
            firestore.collection("users").document(uId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        type =  document.getData().get("type").toString();
                    } else {
                        Log.d("document Not Found", "No such document");
                    }
                } else {
                    Log.d("error", "get failed with ", task.getException());
                }
            }).addOnFailureListener(e -> Log.d("error", e.toString()));

        }

    }
}