package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartActivity extends AppCompatActivity {

    private TextView errorText;
    private ImageButton backButton;
    private AppCompatButton reserve, order;
    private ArrayList<CartModel> productItemData = new ArrayList<>();
    private RelativeLayout cartLayout, errorLayout;
    private RecyclerView cartRecyclerView;
    private CartAdapterRecyclerView adapter;
    FirebaseUser mFirebaseUser;
    String uId = "";
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        CartActivity.this.setTitle("");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        findID();
        if(mFirebaseUser != null){
            uId = mFirebaseUser.getUid();
            cartLayout.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            setCartInfo();

        }else {
            cartLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorText. setText("No data found!");
        }

        backButton.setOnClickListener(view -> onBackPressed());

    }

    private void findID(){
        backButton = findViewById(R.id.back_button);
        reserve = findViewById(R.id.cart_reserve_btn);
        order = findViewById(R.id.cart_order_btn);
        errorText = findViewById(R.id.cart_error_text);
        errorLayout = findViewById(R.id.cart_error_layout);
        cartLayout = findViewById(R.id.cart_items);
        cartRecyclerView = findViewById(R.id.cart_recyclerview);
    }

    private void setAdapter() {
        adapter = new CartAdapterRecyclerView(productItemData);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        cartRecyclerView.setLayoutManager(layoutManager);
        cartRecyclerView.setItemAnimator(new DefaultItemAnimator());
        cartRecyclerView.setAdapter(adapter);
    }

    private void setCartInfo(){
        firestore.collection("users").document(uId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {

                            String id = d.getId();
                            String quntity = d.getData().get("quantity").toString();
                            String type = d.getData().get("type").toString();
                            String name = d.getData().get("name").toString();
                            String image = d.getData().get("image").toString();

                            CartModel data = new CartModel(id, type, quntity, name, image);
                            productItemData.add(data);

                        }
                    } else {
                        cartLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    setAdapter();
                });
    }

    public void ReloadActivity(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}