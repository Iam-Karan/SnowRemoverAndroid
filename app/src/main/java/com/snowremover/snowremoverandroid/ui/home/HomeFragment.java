package com.snowremover.snowremoverandroid.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
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
    private ArrayList<ProductData> copyItemData = new ArrayList<>();
    private RecyclerView homeRecyclerView;
    FirebaseFirestore firestore;
    HomePageRecyclerView adapter;
    private SearchView searchView;
    private ImageButton filterBtn;
    private Spinner dropdownMenu;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firestore = FirebaseFirestore.getInstance();

        homeRecyclerView = root.findViewById(R.id.home_recyclerview);
        searchView = root.findViewById(R.id.home_screen_searchView);
        filterBtn = root.findViewById(R.id.home_screen_filter);
        dropdownMenu = root.findViewById(R.id.home_screen_spinner);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.search(s, copyItemData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.search(s, copyItemData);
                return true;
            }
        });

        filterBtn.setOnClickListener(view -> {
            if(dropdownMenu.getVisibility() == View.VISIBLE){
                dropdownMenu.setVisibility(View.GONE);

            }else {
                dropdownMenu.setVisibility(View.VISIBLE);
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.dropdown_item, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownMenu.setAdapter(spinnerAdapter);
        dropdownMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 2){
                    adapter.available(copyItemData);
                }
                if(i == 3){
                    adapter.lowToHigh(copyItemData);
                }
                if (i == 4){
                    adapter.highToLow(copyItemData);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        productItemData = new ArrayList<>();
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
                            float pricefloat = Float.parseFloat(price);
                            String imageUrl = Objects.requireNonNull(d.getData().get("main_image")).toString();
                            int numOfUnit = Integer.parseInt(d.getData().get("stock_unit").toString());
                            String type = d.getData().get("type").toString();
                            ProductData data = new ProductData(id, name, imageUrl, pricefloat, numOfUnit, type);
                            productItemData.add(data);
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    copyItemData.addAll(productItemData);
                    setAdapter();
                });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}