package com.snowremover.snowremoverandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.snowremover.snowremoverandroid.ui.OrderItemAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderDetailsActivity extends AppCompatActivity {

    Toolbar toolbar;
    String orderID;
    String userName;
    private ImageButton backButton;
    private LinearLayout customerNameLayout;
    private TextView orderIdText, date, address, price, customerFeedback, feedbackLayout, customerName;
    private String orderIdValue, dateValue, addressValue, priceValue, feedbackValue, customerNameValue;
    private AppCompatButton sendFeedback;
    private TextInputEditText feedback;
    private RecyclerView orderDetailsRecyclerView;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore firestore;
    private String uId, customerId;
    private ArrayList<OrderModel> orderData = new ArrayList<>();
    private ArrayList<OrdrItemModel> orderitemtDataArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        OrderDetailsActivity.this.setTitle("");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        orderID = intent.getExtras().getString("orderId");
        userName = intent.getExtras().getString("userName");
        findId();
        firestore = FirebaseFirestore.getInstance();

        if(!userName.equals("admin") ){
            customerNameLayout.setVisibility(View.GONE);
            customerFeedback.setVisibility(View.GONE);
            feedbackLayout.setVisibility(View.GONE);
            feedback.setVisibility(View.VISIBLE);
            sendFeedback.setVisibility(View.VISIBLE);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mAuth.getCurrentUser();
            if(mFirebaseUser != null){
                uId = mFirebaseUser.getUid();
                setData();
            }
        }else {
            customerNameLayout.setVisibility(View.VISIBLE);
            customerFeedback.setVisibility(View.VISIBLE);
            feedbackLayout.setVisibility(View.VISIBLE);
            feedback.setVisibility(View.GONE);
            sendFeedback.setVisibility(View.GONE);
            setAdminAData();
        }

        sendFeedback.setOnClickListener(view -> setFeedback());
        backButton.setOnClickListener(view -> onBackPressed());
    }

    public void findId(){
        orderIdText = findViewById(R.id.order_details_id);
        date = findViewById(R.id.order_details_date);
        address = findViewById(R.id.order_details_address);
        price = findViewById(R.id.order_details_price);
        orderDetailsRecyclerView = findViewById(R.id.order_details_items);
        feedbackLayout = findViewById(R.id.feedback_layout);
        customerNameLayout = findViewById(R.id.customer_name_layout);
        customerName = findViewById(R.id.order_details_customer_name);
        customerFeedback = findViewById(R.id.order_details_customer_feedback);
        sendFeedback = findViewById(R.id.order_details_feedback_btn);
        feedback = findViewById(R.id.order_details_feedback);
        backButton = findViewById(R.id.back_button);
    }

    public void setAdapter(){
        OrderItemAdapter adapter = new OrderItemAdapter(orderitemtDataArrayList, getApplicationContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        orderDetailsRecyclerView.setLayoutManager(layoutManager);
        orderDetailsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        orderDetailsRecyclerView.setAdapter(adapter);
    }

    public void setData(){
        firestore.collection("users").document(uId).collection("order").document(orderID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    DocumentSnapshot d = documentSnapshot;
                    orderIdValue = d.getId();

                    Timestamp date = (Timestamp) d.getData().get("order_date");
                    Date dateDate = date.toDate();
                    dateValue = dateDate.toString();
                    priceValue = d.getData().get("total").toString();
                    addressValue = d.getData().get("address").toString();


                    ArrayList<Map<String, Object>> arrayItem = (ArrayList<Map<String, Object>>) d.getData().get("items");
                    for(int i = 0; i < arrayItem.size(); i++){
                        String itemHour = arrayItem.get(i).get("hour").toString();
                        String itemId = arrayItem.get(i).get("id").toString();
                        String itemImageUrl = arrayItem.get(i).get("imageUrl").toString();
                        String itemName = arrayItem.get(i).get("name").toString();
                        double itemPrice = (double) arrayItem.get(i).get("price");
                        String itemQuantity = arrayItem.get(i).get("quantity").toString();
                        String itemType = arrayItem.get(i).get("type").toString();

                        OrdrItemModel itemData = new OrdrItemModel(itemHour, itemId, itemImageUrl, itemName, itemPrice, itemQuantity, itemType);
                        orderitemtDataArrayList.add(itemData);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    orderIdText.setText(orderIdValue);
                    date.setText(dateValue);
                    price.setText(priceValue);
                    address.setText(addressValue);
                    setAdapter();
                });
    }

    public void setAdminAData(){
        firestore.collection("orders").document(orderID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    DocumentSnapshot d = documentSnapshot;
                    orderIdValue = d.getId();
                    customerId = d.getData().get("userId").toString();
                    Timestamp date = (Timestamp) d.getData().get("order_date");
                    Date dateDate = date.toDate();
                    dateValue = dateDate.toString();
                    priceValue = d.getData().get("total").toString();
                    addressValue = d.getData().get("address").toString();
                    feedbackValue = d.getData().get("feedback").toString();
                    ArrayList<Map<String, Object>> arrayItem = (ArrayList<Map<String, Object>>) d.getData().get("items");
                    for(int i = 0; i < arrayItem.size(); i++){
                        String itemHour = arrayItem.get(i).get("hour").toString();
                        String itemId = arrayItem.get(i).get("id").toString();
                        String itemImageUrl = arrayItem.get(i).get("imageUrl").toString();
                        String itemName = arrayItem.get(i).get("name").toString();
                        double itemPrice = (double) arrayItem.get(i).get("price");
                        String itemQuantity = arrayItem.get(i).get("quantity").toString();
                        String itemType = arrayItem.get(i).get("type").toString();

                        OrdrItemModel itemData = new OrdrItemModel(itemHour, itemId, itemImageUrl, itemName, itemPrice, itemQuantity, itemType);
                        orderitemtDataArrayList.add(itemData);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    orderIdText.setText(orderIdValue);
                    date.setText(dateValue);
                    price.setText(priceValue);
                    address.setText(addressValue);
                    customerFeedback.setText(feedbackValue);
                    setAdapter();
                    firestore.collection("users").document(customerId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document = task1.getResult();
                            assert document != null;
                            if (document.exists()) {
                                customerNameValue = Objects.requireNonNull(document.getData().get("firstName")).toString();

                                customerName.setText(customerNameValue);

                            } else {
                                Log.d("document Not Found", "No such document");
                            }
                        } else {
                            Log.d("error", "get failed with ", task1.getException());
                        }
                    }).addOnFailureListener(e -> Log.d("error", e.toString()));
                });
    }

    public void setFeedback(){
        feedbackValue = feedback.getText().toString();
        if(feedbackValue.isEmpty()){
            feedback.setError("Feedback must not be empty");
        }else{
            firestore.collection("orders").document(orderID).update("feedback", feedbackValue).addOnSuccessListener(unused -> {
                Toast.makeText(getApplicationContext(), "Feedback added successful!", Toast.LENGTH_SHORT).show();
                feedback.setText("");
                feedback.setHint("Feedback");
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }
}