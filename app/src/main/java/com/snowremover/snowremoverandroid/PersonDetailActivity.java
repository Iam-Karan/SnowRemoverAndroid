package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.customer.model.CartModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersonDetailActivity extends AppCompatActivity {

    private TextView personName, personPrice, personDescription, personOrders, personAge, productQuantity;
    private ImageButton addFavourite, removeFavourite, backButton, addtoCart,addProduct, removeProduct;
    private ImageView personImage;
    private AppCompatButton orderProduct, reserveProduct;
    FirebaseUser mFirebaseUser;
    String personId;
    String uId;
    String name;
    double price;
    String imageurl;
    int count = 1;
    private ArrayList<CartModel> cartdata;
    double totalPrice = 0;
    ArrayList<String> personIds = new ArrayList<>();
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        Intent intent = getIntent();
        personId = intent.getExtras().getString("PersonId");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        cartdata = new ArrayList<>();
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
            orderProduct.setOnClickListener(view -> orderProductFunc("order"));
            reserveProduct.setOnClickListener(view -> orderProductFunc("reserve"));
        }else {
            addFavourite.setOnClickListener(view -> toastLogin());

            removeFavourite.setOnClickListener(view -> toastLogin());

            addtoCart.setOnClickListener(view -> toastLogin());
        }

        addProduct.setOnClickListener(view -> {
            count = count + 1;
            productQuantity.setText(""+count);
        });

        removeProduct.setOnClickListener(view -> {
            if(count > 1){
                count = count - 1;
                productQuantity.setText(""+count);
            }
        });

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
        addProduct = findViewById(R.id.product_count_add);
        removeProduct = findViewById(R.id.product_count_remove);
        productQuantity = findViewById(R.id.product_count);
        orderProduct = findViewById(R.id.person_book_order);
        reserveProduct = findViewById(R.id.person_reserve);
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
                    price = Double.parseDouble(document.getData().get("Price").toString());
                    personPrice.setText(document.getData().get("Price").toString());
                    personAge.setText(document.getData().get("age").toString());
                    personOrders.setText(document.getData().get("completed_order").toString());
                } else {
                    Log.d("document Not Found", "No such document");
                }
            } else {
                Log.d("error", "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));

        if(mFirebaseUser !=  null){
            uId = mFirebaseUser.getUid();
            firestore.collection("users").document(uId).collection("favorite").get()
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

            firestore.collection("users").document(uId).collection("cart").document(personId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                count = Integer.parseInt(documentSnapshot.getData().get("hours").toString());
                                productQuantity.setText(""+count);
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
                                    String hour = document.getData().get("hours").toString();
                                    double personPrice = Double.parseDouble(document.getData().get("price").toString());
                                    CartModel data = new CartModel(id, type, quantity, cartName, cartImageurl, hour, personPrice);
                                    personIds.add(data.getId());
                                }
                            }
                            if(personIds.contains(personId)){
                                updateCart();
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

        Map<String, Object> product = new HashMap<>();
        product.put("id", personId);
        product.put("quantity", 1);
        product.put("type", "person");
        product.put("name", name);
        product.put("image", imageurl);
        product.put("hours", count);
        product.put("price", price);
        documentReference.set(product).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), "Person Added successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void updateCart(){
        Map<String, Object> product = new HashMap<>();
        product.put("id", personId);
        product.put("quantity", 1);
        product.put("type", "person");
        product.put("name", name);
        product.put("image", imageurl);
        product.put("hours", count);
        product.put("price", price);
        firestore.collection("users").document(uId).collection("cart").document(personId)
                .update(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Item Added successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

     public void setAddToFavourite(){
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("favorite").document(personId);

        Map<String, Object> product = new HashMap<>();
        product.put("id", personId);
         product.put("quantity", 1);
        product.put("type", "person");
         product.put("name", name);
         product.put("image", imageurl);
        documentReference.set(product).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Item Added successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void setRemoveFavourite(){
        firestore.collection("users").document(uId).collection("favorite").document(personId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Item removed From Favourite successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.w("error", e.toString()));
    }

    public void toastLogin(){
        Toast.makeText(getApplicationContext(), "Login first!", Toast.LENGTH_SHORT).show();
    }


    public void orderProductFunc(String button){
        firestore.collection("users").document(uId).collection("cart").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(Objects.requireNonNull(task.getResult()).size() > 0) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String quantity = document.getData().get("quantity").toString();
                                    String type = document.getData().get("type").toString();
                                    String hour = document.getData().get("hours").toString();
                                    double personPrice = Double.parseDouble(document.getData().get("price").toString());
                                    totalPrice = totalPrice + personPrice * Integer.parseInt(hour);
                                    addToCart();
                                    if(button.equals("order")){
                                        orderIntent( "cart", "cartid", hour);
                                    }else {
                                        reserveIntent( "cart", "cartid", hour);
                                    }
                                }
                            }
                        }else{
                            if(button.equals("order")){
                                orderIntent( "person", personId, String.valueOf(count));
                            }else {
                                reserveIntent("person", personId, String.valueOf(count));
                            }
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "Task Fails to get cart products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void orderIntent(String type, String id, String quantity){
        totalPrice = totalPrice + price * count;
        String totalPriceString = String.valueOf(totalPrice);
        Date currentTime = Calendar.getInstance().getTime();
        Timestamp timestamp = new Timestamp(currentTime);
        String date = timestamp.toDate().toString();
        Intent intent = new Intent(getApplicationContext(), ConfrimOrderActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("date", date);
        intent.putExtra("total", totalPriceString);
        intent.putExtra("id", id);
        intent.putExtra("hours", quantity);
        intent.putExtra("quantity", "1");

        startActivity(intent);
    }
    public void reserveIntent(String type, String id, String quantity){
        totalPrice = totalPrice + price * count;
        String totalPriceString = String.valueOf(totalPrice);
        Date currentTime = Calendar.getInstance().getTime();
        Timestamp timestamp = new Timestamp(currentTime);
        String date = timestamp.toDate().toString();
        Intent intent = new Intent(getApplicationContext(), ReserveActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("date", date);
        intent.putExtra("total", totalPriceString);
        intent.putExtra("id", id);
        intent.putExtra("hours", quantity);
        intent.putExtra("quantity", "1");
        startActivity(intent);
    }
}