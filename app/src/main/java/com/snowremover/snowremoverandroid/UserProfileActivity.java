package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileActivity extends AppCompatActivity {

    private ImageButton backButton, logoutButton, opON, opOFF, npON, npOFF, cpON, cpOFF;
    private TextView email, errortext;
    private EditText name, oldPasword, newPassword, confirmPassword;
    private AppCompatButton updateBtn;
    private FirebaseFirestore firestore;
    private FirebaseUser mFirebaseUser;
    private String uId;
    private String nameText, updateNameText,  oldPasswordText, newPasswordText, confirmPasswordText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        UserProfileActivity.this.setTitle("");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uId = mFirebaseUser.getUid();

        getUi();
        setData();

        updateBtn.setOnClickListener(view -> {
            updateNameText = name.getText().toString();
            oldPasswordText = oldPasword.getText().toString();
            newPasswordText = newPassword.getText().toString();
            confirmPasswordText = confirmPassword.getText().toString();

            if(!updateNameText.equals(nameText)){
                UpdateName();
            }
            if(!oldPasswordText.isEmpty()){
                ValidatePassword();
            }
        });

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        });
        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        });

        opON.setOnClickListener(view -> {
            oldPasword.setTransformationMethod(null);
            opON.setVisibility(View.GONE);
            opOFF.setVisibility(View.VISIBLE);
        });
        opOFF.setOnClickListener(view -> {
            oldPasword.setTransformationMethod(new PasswordTransformationMethod());
            opON.setVisibility(View.VISIBLE);
            opOFF.setVisibility(View.GONE);
        });
        npON.setOnClickListener(view -> {
            newPassword.setTransformationMethod(null);
            npON.setVisibility(View.GONE);
            npOFF.setVisibility(View.VISIBLE);
        });
        npOFF.setOnClickListener(view -> {
            newPassword.setTransformationMethod(new PasswordTransformationMethod());
            npON.setVisibility(View.VISIBLE);
            npOFF.setVisibility(View.GONE);
        });
        cpON.setOnClickListener(view -> {
            confirmPassword.setTransformationMethod(null);
            cpON.setVisibility(View.GONE);
            cpOFF.setVisibility(View.VISIBLE);
        });
        cpOFF.setOnClickListener(view -> {
            confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
            cpON.setVisibility(View.VISIBLE);
            cpOFF.setVisibility(View.GONE);
        });
    }

    public void getUi(){
        backButton = findViewById(R.id.back_button);
        logoutButton = findViewById(R.id.logout_btn);
        name = findViewById(R.id.user_name);
        email = findViewById(R.id.user_email);
        oldPasword = findViewById(R.id.user_old_password);
        newPassword = findViewById(R.id.user_new_password);
        confirmPassword = findViewById(R.id.user_confirm_password);
        updateBtn = findViewById(R.id.user_update);
        errortext = findViewById(R.id.user_profile_error);
        opON = findViewById(R.id.old_password_visibility_on);
        opOFF = findViewById(R.id.old_password_visibility_off);
        npON = findViewById(R.id.new_password_visibility_on);
        npOFF = findViewById(R.id.new_password_visibility_off);
        cpON = findViewById(R.id.confirm_password_visibility_on);
        cpOFF = findViewById(R.id.confirm_password_visibility_off);
    }

    public void setData(){
        firestore.collection("users").document(uId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    nameText = Objects.requireNonNull(document.getData().get("firstName")).toString();
                    String emailText = Objects.requireNonNull(document.getData().get("email")).toString();

                    name.setText(nameText);
                    email.setText(emailText);

                } else {
                    Log.d("document Not Found", "No such document");
                }
            } else {
                Log.d("error", "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));

    }

    public void UpdateName(){
        if(!updateNameText.isEmpty()){
            firestore.collection("users").document(uId)
                    .update("firstName", updateNameText)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getApplicationContext(), "Name updated successful!", Toast.LENGTH_SHORT).show();
                        setData();
                    }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }else {
            errortext.setText("Name must not be empty");
            errortext.setVisibility(View.VISIBLE);
            name.setError("Name must not be empty!");
        }
    }

    public void ValidatePassword(){
        if(isValidPassword(newPasswordText)){
            if(newPasswordText.equals(confirmPasswordText)){
                UpdatePasword();
            } else {
                errortext.setText("Confirm password not match");
                errortext.setVisibility(View.VISIBLE);
                confirmPassword.setError("Confirm password not match!");
            }
        }else {
            errortext.setText("New password is too weak");
            errortext.setVisibility(View.VISIBLE);
            newPassword.setError("New password must not be empty!");
        }
    }

    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public void UpdatePasword(){
        String emailText = email.getText().toString();
        oldPasswordText = oldPasword.getText().toString();
        AuthCredential credential = EmailAuthProvider
                .getCredential(emailText, oldPasswordText);

        mFirebaseUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        mFirebaseUser.updatePassword(newPasswordText)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                         Toast.makeText(getBaseContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                         finish();
                                         overridePendingTransition( 0, 0);
                                         startActivity(getIntent());
                                         overridePendingTransition( 0, 0);
                                    } else {
                                        Toast.makeText(getBaseContext(), "Error password not updated!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        errortext.setText("Wrong old Password");
                        errortext.setVisibility(View.VISIBLE);
                        oldPasword.setError("Wrong Password!");
                    }
                });

    }
}