package com.snowremover.snowremoverandroid.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.HomePageRecyclerView;
import com.snowremover.snowremoverandroid.ProductData;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ArrayList<ProductData> productItemData = new ArrayList<>();
    private RecyclerView homeRecyclerView;
    FirebaseFirestore firestore;
    HomePageRecyclerView adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firestore = FirebaseFirestore.getInstance();

        homeRecyclerView = root.findViewById(R.id.home_recyclerview);
        productItemData = new ArrayList<ProductData>();
        setProductsInfo();

        return root;
    }

    private void setAdapter() {
        adapter = new HomePageRecyclerView(productItemData);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        homeRecyclerView.setLayoutManager(layoutManager);
        homeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        homeRecyclerView.setAdapter(adapter);
    }


    private void setProductsInfo() {
        firestore.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            String id = d.getId().toString();
                            String name = Objects.requireNonNull(d.getData().get("name")).toString();
                            String price = Objects.requireNonNull(d.getData().get("price_numerical")).toString();
                            String imageUrl = Objects.requireNonNull(d.getData().get("main_image")).toString();
                            ProductData data = new ProductData(id, name, imageUrl, price);
                            productItemData.add(data);
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> setAdapter());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}