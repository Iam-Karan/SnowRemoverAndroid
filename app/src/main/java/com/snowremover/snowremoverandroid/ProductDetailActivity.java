package com.snowremover.snowremoverandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.snowremover.snowremoverandroid.customer.model.CartModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView productName, productPrice, productDescription, productQuantity, videoBtn;
    private ImageButton addFavourite, removeFavourite, addProduct, removeProduct, backButton, addtoCart;
    private ImageView productImage;
    private AppCompatButton orderProduct, reserveProduct;
    private YouTubePlayerView youTubePlayerView;
    private ArrayList<CartModel> cartdata;
    String youtubeUrl = "https://www.youtube.com/watch?v=";
    String url;
    String finalUrl;
    FirebaseUser mFirebaseUser;
    String prductId;
    String uId;
    int cartCount = 0;
    int count = 1;
    String name;
    double price;
    double totalPrice = 0;
    String imageurl;
    ArrayList<String> productIds = new ArrayList<>();
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent = getIntent();
        prductId = intent.getExtras().getString("ProductId");

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

        videoBtn.setOnClickListener(view -> {
            productImage.setVisibility(View.GONE);
            youTubePlayerView.setVisibility(View.VISIBLE);
        });

        backButton.setOnClickListener(view -> onBackPressed());

    }
    public void findID(){
        backButton = findViewById(R.id.back_button);
        productName = findViewById(R.id.product_name);
        productDescription = findViewById(R.id.product_description);
        productPrice = findViewById(R.id.product_price);
        productQuantity = findViewById(R.id.product_count);
        addFavourite = findViewById(R.id.product_add_favourite);
        removeFavourite = findViewById(R.id.product_remove_favourite);
        addProduct = findViewById(R.id.product_count_add);
        removeProduct = findViewById(R.id.product_count_remove);
        productImage = findViewById(R.id.product_image);
        videoBtn = findViewById(R.id.watch_video);
        youTubePlayerView = findViewById(R.id.product_video);
        addtoCart = findViewById(R.id.product_add_to_cart);
        orderProduct = findViewById(R.id.product_order);
        reserveProduct = findViewById(R.id.product_reserve);
        getLifecycle().addObserver(youTubePlayerView);
    }

    public void setData(){

        DocumentReference docRef = firestore.collection("products").document(prductId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    StorageReference storageReference =  FirebaseStorage.getInstance().getReference("products/"+document.getData().get("main_image").toString());

                    storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                            Glide.with(this)
                                    .load(uri)
                                    .into(productImage));
                    youtubeUrl = "https://www.youtube.com/watch?v=";
                    url = document.getData().get("video_url").toString();
                    finalUrl = url.replace(youtubeUrl, "");

                    youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                        @Override
                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                            youTubePlayer.cueVideo(finalUrl, 0);
                        }
                    });
                    name = document.getData().get("name").toString();
                    imageurl = "products/"+document.getData().get("main_image");
                    productName.setText(document.getData().get("name").toString());
                    productDescription.setText(document.getData().get("description").toString());
                    price = Double.parseDouble(document.getData().get("price_numerical").toString());
                    productPrice.setText("$"+document.getData().get("price_numerical").toString());
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
                                        if(prductId.equals(document.getId())){
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

            firestore.collection("users").document(uId).collection("cart").document(prductId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                count = Integer.parseInt(documentSnapshot.getData().get("quantity").toString());
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
                                    String name = document.getData().get("name").toString();
                                    String image = document.getData().get("image").toString();
                                    String hour = document.getData().get("hours").toString();
                                    double personPrice = Double.parseDouble(document.getData().get("price").toString());
                                    CartModel data = new CartModel(id, type, quantity, name, image, hour, price);
                                    cartdata.add(data);
                                    if(id.equals(prductId)){
                                        cartCount = Integer.parseInt(quantity);
                                    }
                                    productIds.add(data.getId());
                                }
                            }
                            if(productIds.contains(prductId)){
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
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("cart").document(prductId);

        Map<String, Object> product = new HashMap<>();
        product.put("id", prductId);
        product.put("quantity", count);
        product.put("type", "products");
        product.put("name", name);
        product.put("image", imageurl);
        product.put("hours", 1);
        product.put("price", price);
        documentReference.set(product).addOnSuccessListener(unused -> {
            Toast.makeText(getApplicationContext(), "Item Added successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void updateCart(){
        Map<String, Object> product = new HashMap<>();
        product.put("id", prductId);
        product.put("quantity", count);
        product.put("type", "products");
        product.put("name", name);
        product.put("image", imageurl);
        product.put("hours", 1);
        product.put("price", price);
        firestore.collection("users").document(uId).collection("cart").document(prductId)
                .update(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Cart Updated successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void setAddToFavourite(){
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("favorite").document(prductId);

        Map<String, String> product = new HashMap<>();
        product.put("id", prductId);
        product.put("type", "products");
        documentReference.set(product).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Item Added successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
    }

    public void setRemoveFavourite(){
        firestore.collection("users").document(uId).collection("favorite").document(prductId)
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
                                    double personPrice = Double.parseDouble(document.getData().get("price").toString());
                                    totalPrice = totalPrice + personPrice * Integer.parseInt(quantity);
                                    addToCart();
                                    if(button.equals("order")){
                                        orderIntent( "cart", "cartid", quantity);
                                    }else {
                                        reserveIntent( "cart", "cartid", quantity);
                                    }
                                }
                            }
                        }
                        else{
                            if(button.equals("order")){
                                Log.d("order product", "button else clicked");
                                orderIntent( "products", prductId, String.valueOf(count));
                            }else {
                                reserveIntent("products", prductId, String.valueOf(count));
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
        Log.d("order product", "intent clicked");
        Date currentTime = Calendar.getInstance().getTime();
        Timestamp timestamp = new Timestamp(currentTime);
        String date = timestamp.toDate().toString();
        Intent intent = new Intent(getApplicationContext(), ConfrimOrderActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("date", date);
        intent.putExtra("total", totalPriceString);
        intent.putExtra("id", id);
        intent.putExtra("hours", "1");
        intent.putExtra("quantity", quantity);
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
        intent.putExtra("hours", "1");
        intent.putExtra("quantity", quantity);
        startActivity(intent);
    }
}