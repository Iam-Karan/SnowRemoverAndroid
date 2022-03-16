package com.snowremover.snowremoverandroid.ui.order;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snowremover.snowremoverandroid.HomeScreen;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.SignInScreen;
import com.snowremover.snowremoverandroid.SignUpScreen;
import com.snowremover.snowremoverandroid.UserProfileActivity;

public class OrderFragment extends Fragment {

    private OrderViewModel mViewModel;
    private LinearLayout userInfoLayout;
    private TextView userName;
    private ImageButton cartBtn;

    public static OrderFragment newInstance() {
        return new OrderFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        getUi(view);
        userInfoLayout.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), SignInScreen.class);
            startActivity(intent);
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
    }

}