package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpScreen extends AppCompatActivity {

    private EditText name, email, password, confirmPassword;
    private AppCompatButton signUpBtn;
    private GoogleSignInButton signupWithGoogle;
    private TextView loginBtn, errorText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String uId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        findId();

        signUpBtn.setOnClickListener(view -> {
            validate();
        });

        loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), SignInScreen.class);
            startActivity(intent);
        });

    }

    public void findId(){
        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.signup_confirm_password);
        signUpBtn = findViewById(R.id.signup_btn);
        signupWithGoogle = findViewById(R.id.signup_with_google);
        loginBtn = findViewById(R.id.signin_btn);
        errorText = findViewById(R.id.signup_error_text);
    }

    public void validate(){
        String nameToast = "Name must not be empty.";
        String emailToast = "Invalid email.";
        String emptyPasswordToast = "Password is not valid.";
        String passwordToast = "Password and confirm Password is not match";


        String nameText = (name.getText()).toString();
        String emailText = (email.getText()).toString();
        String passwordText = (password.getText()).toString();
        String confirmPasswordText = (confirmPassword.getText()).toString();

        if (!nameText.isEmpty()){
            if(isValidEmailAddress(emailText)){
                if(isValidPassword(passwordText)){
                    if(passwordText.equals(confirmPasswordText)){
                        creatUser(nameText, emailText, passwordText);
                    }
                    else {
                        confirmPassword.setError(passwordToast);
                    }
                }
                else {
                    password.setError(emptyPasswordToast);
                }
            }
            else {
                email.setError(emailToast);
            }
        } else {
            name.setError(nameToast);
        }
    }

    public boolean isValidEmailAddress(String emailID) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        Pattern p = java.util.regex.Pattern.compile(ePattern);
        Matcher m = p.matcher(emailID);
        if(!Objects.requireNonNull(email.getText()).toString().isEmpty()){
            return m.matches();
        }
        return false;
    }

    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public void creatUser(String name, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(task -> {
            FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
            if(mFirebaseUser != null) {
                uId = mFirebaseUser.getUid();
                DocumentReference documentReference = firestore.collection("users").document(uId);

                Map<String, String> user = new HashMap<>();
                user.put("name", name);
                user.put("email", email);
                user.put("uid", uId);

                documentReference.set(user).addOnSuccessListener(unused -> {
                    Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                    startActivity(intent);
                }).addOnFailureListener(e -> setToast(e.toString()));
            }
        }).addOnFailureListener(e -> setToast(e.toString()));
    }

    public void setToast(String msg){
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}