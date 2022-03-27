package com.snowremover.snowremoverandroid.ui.order;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.CartActivity;
import com.snowremover.snowremoverandroid.CartModel;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.SignInScreen;
import com.snowremover.snowremoverandroid.UserProfileActivity;

import java.util.ArrayList;
import java.util.Objects;

public class OrderFragment extends Fragment {

    private OrderViewModel mViewModel;
    private LinearLayout userInfoLayout;
    private TextView userName, numberOfItemCart;
    private ImageButton cartBtn;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore firestore;
    private String uId;
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
        }

        getUi(view);

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CartActivity.class);
                startActivity(intent);
            }
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

}