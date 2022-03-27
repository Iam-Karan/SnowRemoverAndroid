package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersonDetailActivity extends AppCompatActivity {

    private TextView personName, personPrice, personDescription, personOrders, personAge;
    private ImageButton addFavourite, removeFavourite, backButton;
    private AppCompatButton addtoCart;
    private ImageView personImage;
    FirebaseUser mFirebaseUser;
    String personId;
    String uId;
    String name;
    String imageurl;
    ArrayList<String> personIds = new ArrayList<>();
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        PersonDetailActivity.this.setTitle("");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        personId = intent.getExtras().getString("PersonId");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        findID();
        setData();

        if(mFirebaseUser != null){
            uId = mFirebaseUser.getUid();
            addFavourite.setOnClickListener(view -> {
                setAddToFavourite();
                removeFavourite.setVisibility(View.VISIBLE);
                addFavourite.setVisibility(View.GONE);
            });

            removeFavourite.setOnClickListener(view -> {
                setRemoveFavourite();
                addFavourite.setVisibility(View.VISIBLE);
                removeFavourite.setVisibility(View.GONE);
            });

            addtoCart.setOnClickListener(view -> addToCart());
        }else {
            addFavourite.setOnClickListener(view -> toastLogin());

            removeFavourite.setOnClickListener(view -> toastLogin());

            addtoCart.setOnClickListener(view -> toastLogin());
        }

        backButton.setOnClickListener(view -> {
            onBackPressed();
        });
    }
    public void findID(){
        backButton = findViewById(R.id.back_button);
        personName = findViewById(R.id.person_name);
        personDescription = findViewById(R.id.person_description);
        personPrice = findViewById(R.id.person_price);
        addFavourite = findViewById(R.id.person_add_favourite);
        removeFavourite = findViewById(R.id.person_remove_favourite);
        personImage = findViewById(R.id.person_image);
        personAge = findViewById(R.id.person_age);
        personOrders = findViewById(R.id.person_order);
        addtoCart = findViewById(R.id.person_add_to_cart);
    }

    public void setData(){


        DocumentReference docRef = firestore.collection("person").document(personId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    StorageReference storageReference =  FirebaseStorage.getInstance().getReference("personimages/"+document.getData().get("imageurl").toString());

                    storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                            Glide.with(this)
                                    .load(uri)
                                    .into(personImage));

                    name = document.getData().get("name").toString();
                    imageurl = "personimages/"+document.getData().get("imageurl");
                    personName.setText(document.getData().get("name").toString());
                    personDescription.setText(document.getData().get("description").toString());
                    personPrice.setText("$ 0.00");
                    personAge.setText(document.getData().get("age").toString());
                    personOrders.setText(document.getData().get("numberOfOrder").toString());
                } else {
                    Log.d("document Not Found", "No such document");
                }
            } else {
                Log.d("error", "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));

        if(mFirebaseUser !=  null){
            uId = mFirebaseUser.getUid();
            firestore.collection("users").document(uId).collection("favourite").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(Objects.requireNonNull(task.getResult()).size() > 0) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        if(personId.equals(document.getId())){
                                            removeFavourite.setVisibility(View.VISIBLE);
                                            addFavourite.setVisibility(View.GONE);
                                        }
                                    }

                                }
                            } else {
                                removeFavourite.setVisibility(View.GONE);
                                addFavourite.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Task Fails to get Favourite products", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @SuppressLint("SetTextI18n")
    public void addToCart(){

        firestore.collection("users").document(uId).collection("cart").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(Objects.requireNonNull(task.getResult()).size() > 0) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String id = document.getData().get("id").toString();
                                    String type = document.getData().get("type").toString();
                                    String quantity = document.getData().get("quantity").toString();
                                    String cartName = document.getData().get("name").toString();
                                    String cartImageurl = "personimages/"+document.getData().get("imageurl");
                                    CartModel data = new CartModel(id, type, quantity, cartName, cartImageurl);
                                    personIds.add(data.getId());
                                }
                            }
                            if(personIds.contains(personId)){
                                Toast.makeText(getApplicationContext(), "Already added in the cart!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                addProductToCart();
                            }
                        } else {
                            addProductToCart();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Task Fails to get cart products", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @SuppressLint("SetTextI18n")
    public void addProductToCart(){
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("cart").document(personId);

        Map<String, String> product = new HashMap<>();
        product.put("id", personId);
        product.put("quantity", "1");
        product.put("type", "person");
        product.put("name", name);
        product.put("image", imageurl);
        documentReference.set(product).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), "Person Added successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

     public void setAddToFavourite(){
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("favourite").document(personId);

        Map<String, String> product = new HashMap<>();
        product.put("id", personId);
         product.put("quantity", "1");
        product.put("type", "person");
         product.put("name", name);
         product.put("image", imageurl);
        documentReference.set(product).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Item Added successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void setRemoveFavourite(){
        firestore.collection("users").document(uId).collection("favourite").document(personId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Item removed From Favourite successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.w("error", e.toString()));
    }

    public void toastLogin(){
        Toast.makeText(getApplicationContext(), "Login first!", Toast.LENGTH_SHORT).show();
    }
}