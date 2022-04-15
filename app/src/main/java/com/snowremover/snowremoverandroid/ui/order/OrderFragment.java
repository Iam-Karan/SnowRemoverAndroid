package com.snowremover.snowremoverandroid.ui.order;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.CartActivity;
import com.snowremover.snowremoverandroid.CartAdapterRecyclerView;
import com.snowremover.snowremoverandroid.CartModel;
import com.snowremover.snowremoverandroid.OrderAdapter;
import com.snowremover.snowremoverandroid.OrderModel;
import com.snowremover.snowremoverandroid.OrdrItemModel;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.SignInScreen;
import com.snowremover.snowremoverandroid.UserProfileActivity;
import com.snowremover.snowremoverandroid.ui.OrderItemAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private OrderViewModel mViewModel;
    private LinearLayout userInfoLayout;
    private TextView userName, numberOfItemCart;
    private ImageButton cartBtn;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore firestore;
    private String uId;
    private ArrayList<OrderModel> orderData = new ArrayList<>();
    private boolean isOrderd = true;
    private RecyclerView ordrRecyclerView;
    private OrderAdapter adapter;
    ArrayList<String> productIds = new ArrayList<>();

    public static OrderFragment newInstance() {
        return new OrderFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null){
            firestore = FirebaseFirestore.getInstance();
            uId = mFirebaseUser.getUid();
            setData();
            setOrder();
        }

        getUi(view);

        cartBtn.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        userInfoLayout.setOnClickListener(view1 -> {
            if(mFirebaseUser != null){
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(getContext(), SignInScreen.class);
                startActivity(intent);
            }

        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        // TODO: Use the ViewModel
    }

    public void getUi(View view){
        userInfoLayout = view.findViewById(R.id.user_profile_layout);
        userName = view.findViewById(R.id.order_user_name);
        cartBtn = view.findViewById(R.id.order_cart_btn);
        numberOfItemCart = view.findViewById(R.id.order_cart_item_number);
        ordrRecyclerView = view.findViewById(R.id.order_recyclerview);
    }

    private void setAdapter() {
        adapter = new OrderAdapter(orderData, getContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ordrRecyclerView.setLayoutManager(layoutManager);
        ordrRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ordrRecyclerView.setAdapter(adapter);
    }

    public void setData(){
        firestore.collection("users").document(uId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    String nameText = Objects.requireNonNull(document.getData().get("firstName")).toString();

                    userName.setText(nameText);

                } else {
                    Log.d("document Not Found", "No such document");
                }
            } else {
                Log.d("error", "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));

        firestore.collection("users").document(uId).collection("cart").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(Objects.requireNonNull(task.getResult()).size() > 0) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Log.d("productId", document.getId());
                                    productIds.add(document.getId());
                                }
                            }
                            if(productIds.size() > 0){
                                numberOfItemCart.setVisibility(View.VISIBLE);
                                numberOfItemCart.setText(""+productIds.size());
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Task Fails to get cart products", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void setOrder(){
        firestore.collection("users").document(uId).collection("order").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            ArrayList<OrdrItemModel> orderitemtDataArrayList = new ArrayList<>();
                            String id = d.getId();
                            Timestamp date = (Timestamp) d.getData().get("order_date");
                            Date dateDate = date.toDate();
                            String dateString = dateDate.toString();
                            String price = d.getData().get("total").toString();
                            String imageUrl = "https://i.dlpng.com/static/png/6728131_preview.png";

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
                            int count = arrayItem.size();
                            String countString = String.valueOf(count);

                            OrderModel data = new OrderModel(id, dateString, countString, price, imageUrl, isOrderd, orderitemtDataArrayList);
                            orderData.add(data);
                            isOrderd = !isOrderd;
                        }

                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> setAdapter());
    }

}