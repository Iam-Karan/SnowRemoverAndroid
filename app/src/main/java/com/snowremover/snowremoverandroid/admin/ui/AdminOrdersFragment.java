package com.snowremover.snowremoverandroid.admin.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

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
import com.snowremover.snowremoverandroid.HomeScreen;
import com.snowremover.snowremoverandroid.customer.adapter.OrderAdapter;
import com.snowremover.snowremoverandroid.customer.model.OrderModel;
import com.snowremover.snowremoverandroid.customer.model.OrdrItemModel;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.UserProfileActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminOrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminOrdersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private LinearLayout userInfoLayout;
    private TextView userName;
    private ImageButton signoutBtn;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore firestore;
    private String uId;
    private final String userNameValue = "admin";
    private ArrayList<OrderModel> orderData = new ArrayList<>();
    private boolean isOrderd = true;
    private RecyclerView ordrRecyclerView;
    private OrderAdapter adapter;

    public AdminOrdersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminOrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminOrdersFragment newInstance(String param1, String param2) {
        AdminOrdersFragment fragment = new AdminOrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);
        getUi(view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        if(mFirebaseUser != null){
            firestore = FirebaseFirestore.getInstance();
            uId = mFirebaseUser.getUid();
            setData();
            setOrder();
        }

        signoutBtn.setOnClickListener(view1 -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Sign out successfully!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getContext(), HomeScreen.class);
            startActivity(intent);
        });

        userInfoLayout.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            startActivity(intent);
        });


        return view;
    }
    public void getUi(View view){
        userInfoLayout = view.findViewById(R.id.user_profile_layout);
        userName = view.findViewById(R.id.order_user_name);
        signoutBtn = view.findViewById(R.id.admin_signout);
        ordrRecyclerView = view.findViewById(R.id.order_recyclerview);
    }

    private void setAdapter() {
        adapter = new OrderAdapter(orderData, getContext(), userNameValue);
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
    }

    public void setOrder(){
        ProgressDialog progressdialog = new ProgressDialog(getContext());
        progressdialog.show();
        firestore.collection("orders").get()
                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (!queryDocumentSnapshots1.isEmpty()) {

                        List<DocumentSnapshot> list1 = queryDocumentSnapshots1.getDocuments();
                        for (DocumentSnapshot d : list1) {
                            ArrayList<OrdrItemModel> orderitemtDataArrayList = new ArrayList<>();
                            String id = d.getId();

                            Timestamp date = (Timestamp) d.getData().get("order_date");
                            Date dateDate = date.toDate();
                            String dateString = dateDate.toString();
                            String price = d.getData().get("total").toString();
                            String imageUrl = "https://i.dlpng.com/static/png/6728131_preview.png";
                            isOrderd = (boolean) d.getData().get("isDelivered");
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
                        }

                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressdialog.dismiss();
                    setAdapter();
                });

    }
}