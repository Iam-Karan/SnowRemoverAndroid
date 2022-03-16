package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.Objects;

public class SignInScreen extends AppCompatActivity {

    private EditText email, password;
    private AppCompatButton signin;
    private GoogleSignInButton googleSignInButton;
    private TextView errorText, signupBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        mAuth = FirebaseAuth.getInstance();
        findID();

        signin.setOnClickListener(view -> validate());
        signupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), SignUpScreen.class);
            startActivity(intent);
        });
    }

    public void findID(){
        email = findViewById(R.id.signin_email);
        password = findViewById(R.id.signin_password);
        signin = findViewById(R.id.login_btn);
        googleSignInButton = findViewById(R.id.login_with_google);
        errorText = findViewById(R.id.signin_invalid_signin);
        signupBtn = findViewById(R.id.signup_btn);
    }

    public void validate(){
        String emailText = Objects.requireNonNull(email.getText()).toString();
        String passwordText = Objects.requireNonNull(password.getText()).toString();
        if(emailText.isEmpty() || passwordText.isEmpty()){
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        loginUser(emailText, passwordText);
        errorText.setVisibility(View.GONE);

    }

    public void loginUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            errorText.setVisibility(View.GONE);
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        }).addOnFailureListener(e -> errorText.setVisibility(View.VISIBLE));
    }
}