package com.snowremover.snowremoverandroid.admin.ui;

import android.content.Intent;
import android.os.Bundle;

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
import com.snowremover.snowremoverandroid.HomeScreen;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.SignInScreen;
import com.snowremover.snowremoverandroid.UserProfileActivity;

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
        uId = mFirebaseUser.getUid();
        setData();

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
}