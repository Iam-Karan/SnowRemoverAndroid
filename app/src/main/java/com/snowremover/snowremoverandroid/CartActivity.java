package com.snowremover.snowremoverandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.customer.adapter.CartAdapterRecyclerView;
import com.snowremover.snowremoverandroid.customer.model.CartModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private TextView errorText;
    private ImageButton backButton;
    private AppCompatButton reserve, order;
    private ArrayList<CartModel> productItemData = new ArrayList<>();
    private ArrayList<CartModel> copyData = new ArrayList<>();
    private RelativeLayout cartLayout, errorLayout;
    private RecyclerView cartRecyclerView;
    private CartAdapterRecyclerView adapter;
    double totalPrice = 0;
    FirebaseUser mFirebaseUser;
    String uId = "";
    FirebaseFirestore firestore;
    Date currentTime;
    Timestamp timestamp;
    String date;

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

            order.setOnClickListener(view -> orderProduct());
            reserve.setOnClickListener(view -> reserveProduct());

        }else {
            cartLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        }

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), HomeScreen.class);
            startActivity(intent);
        });
    }

    private void findID(){
        backButton = findViewById(R.id.back_button);
        reserve = findViewById(R.id.cart_reserve_btn);
        order = findViewById(R.id.cart_order_btn);
        errorText = findViewById(R.id.cart_error_text);
        errorLayout = findViewById(R.id.cart_error_layout);
        cartLayout = findViewById(R.id.cart_items);
        cartRecyclerView = findViewById(R.id.cart_recyclerview);

        currentTime = Calendar.getInstance().getTime();
        timestamp = new Timestamp(currentTime);
        date = timestamp.toDate().toString();
    }

    private void setAdapter() {
        adapter = new CartAdapterRecyclerView(productItemData, copyData);
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
                            String hour = d.getData().get("hours").toString();
                            double personPrice = Double.parseDouble(d.getData().get("price").toString());
                            totalPrice = totalPrice + personPrice;
                            CartModel data = new CartModel(id, type, quntity, name, image, hour, personPrice);
                            productItemData.add(data);
                        }
                        copyData.addAll(productItemData);
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

    public void reserveProduct(){
        Intent intent = new Intent(getApplicationContext(), ReserveActivity.class);
        intent.putExtra("type", "cart");
        intent.putExtra("date", date);
        intent.putExtra("total", String.valueOf(totalPrice));
        intent.putExtra("id", "cartid");
        intent.putExtra("hours", "1");
        intent.putExtra("quantity", "1");
        startActivity(intent);
    }

    public void orderProduct(){
        Intent intent = new Intent(getApplicationContext(), ConfrimOrderActivity.class);
        intent.putExtra("type", "cart");
        intent.putExtra("date", date);
        intent.putExtra("total", String.valueOf(totalPrice));
        intent.putExtra("id", "cartid");
        intent.putExtra("hours", "1");
        intent.putExtra("quantity", "1");
        startActivity(intent);

    }

}