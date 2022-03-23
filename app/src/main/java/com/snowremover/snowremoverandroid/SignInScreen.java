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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignInScreen extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;

    private EditText email, password;
    private AppCompatButton signin;
    private GoogleSignInButton googleSignInButton;
    private TextView errorText, signupBtn;
    private ImageView passwordVisible, passwordInVisible;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        findID();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1050868759487-pd1g13mjamlmunstg5pboetrae94mtab.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(view -> signIn());

        signin.setOnClickListener(view -> validate());
        signupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), SignUpScreen.class);
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
    }

    public void findID(){
        email = findViewById(R.id.signin_email);
        password = findViewById(R.id.signin_password);
        signin = findViewById(R.id.login_btn);
        googleSignInButton = findViewById(R.id.login_with_google);
        errorText = findViewById(R.id.signin_invalid_signin);
        signupBtn = findViewById(R.id.signup_btn);
        passwordVisible = findViewById(R.id.signin_password_visibility_on);
        passwordInVisible = findViewById(R.id.signin_password_visibility_off);
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
            Toast.makeText(getApplicationContext(), "Login successfully!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        }).addOnFailureListener(e -> errorText.setVisibility(View.VISIBLE));
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