package com.snowremover.snowremoverandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpScreen extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;

    private EditText name, email, password, confirmPassword;
    private AppCompatButton signUpBtn;
    private GoogleSignInButton signupWithGoogle;
    private TextView loginBtn, errorText;
    private ImageView passwordVisible, passwordInVisible, confirmPasswordVisible, confirmPasswordInVisible;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;
    private String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        findId();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1050868759487-pd1g13mjamlmunstg5pboetrae94mtab.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signupWithGoogle.setOnClickListener(view -> signIn());

        signUpBtn.setOnClickListener(view -> {
            validate();
        });

        loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), SignInScreen.class);
            startActivity(intent);
        });

        passwordVisible.setOnClickListener(view -> {
            password.setTransformationMethod(null);
            passwordVisible.setVisibility(View.GONE);
            passwordInVisible.setVisibility(View.VISIBLE);
        });

        passwordInVisible.setOnClickListener(view -> {
            password.setTransformationMethod(new PasswordTransformationMethod());
            passwordVisible.setVisibility(View.VISIBLE);
            passwordInVisible.setVisibility(View.GONE);
        });

        confirmPasswordVisible.setOnClickListener(view -> {
            confirmPassword.setTransformationMethod(null);
            confirmPasswordInVisible.setVisibility(View.VISIBLE);
            confirmPasswordVisible.setVisibility(View.GONE);
        });

        confirmPasswordInVisible.setOnClickListener(view -> {
            confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
            confirmPasswordVisible.setVisibility(View.VISIBLE);
            confirmPasswordInVisible.setVisibility(View.GONE);
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
        passwordVisible = findViewById(R.id.signup_password_visibility_on);
        passwordInVisible = findViewById(R.id.signup_password_visibility_off);
        confirmPasswordVisible = findViewById(R.id.signup_confirm_password_visibility_on);
        confirmPasswordInVisible = findViewById(R.id.signup_confirm_password_visibility_off);
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
                        errorText.setText(passwordToast);
                        errorText.setVisibility(View.VISIBLE);
                        confirmPassword.setError(passwordToast);
                    }
                }
                else {
                    errorText.setText(emptyPasswordToast);
                    errorText.setVisibility(View.VISIBLE);
                    password.setError(emptyPasswordToast);
                }
            }
            else {
                errorText.setText(emailToast);
                errorText.setVisibility(View.VISIBLE);
                email.setError(emailToast);
            }
        } else {
            errorText.setText(nameToast);
            errorText.setVisibility(View.VISIBLE);
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
                user.put("firstName", name);
                user.put("email", email);
                user.put("uid", uId);
                user.put("type", "Customer");

                documentReference.set(user).addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Sign Up successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getBaseContext(), SignInScreen.class);
                    startActivity(intent);
                }).addOnFailureListener(e -> {
                    setToast(e.toString());
                    errorText.setText("Email id already used!");
                    errorText.setVisibility(View.VISIBLE);
                });
            }
        }).addOnFailureListener(e -> setToast(e.toString()));
    }

    public void setToast(String msg){
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("requestcode", ""+requestCode);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken(), account.getGivenName(), account.getEmail());
        } catch (ApiException e) {
            Log.w("TAG", "Google sign in failed", e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String name, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        DocumentReference documentReference = firestore.collection("users").document(userId);
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = (DocumentSnapshot) task.getResult();
                                    if (!document.exists()) {
                                        DocumentReference documentReference = firestore.collection("users").document(userId);

                                        Map<String, String> userGoogle = new HashMap<>();
                                        userGoogle.put("firstName", name);
                                        userGoogle.put("email", email);
                                        userGoogle.put("uid", userId);
                                        userGoogle.put("type", "Customer");

                                        documentReference.set(userGoogle).addOnSuccessListener(unused -> {
                                            Toast.makeText(getApplicationContext(), "Login successfully!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                                            startActivity(intent);
                                        }).addOnFailureListener(e -> Log.d("error", e.toString()));
                                    }
                                    Toast.makeText(getApplicationContext(), "Login successfully!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                                    startActivity(intent);

                                } else {
                                    Log.d("error", "get failed with ", task.getException());
                                }
                            }
                        }).addOnFailureListener(e -> Log.d("error", e.toString()));

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                    }
                });
    }
}