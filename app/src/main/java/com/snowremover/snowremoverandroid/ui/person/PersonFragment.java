package com.snowremover.snowremoverandroid.ui.person;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.HomePageRecyclerView;
import com.snowremover.snowremoverandroid.ProductData;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonFragment extends Fragment {

    private PersonViewModel mViewModel;
    private ArrayList<ProductData> productItemData = new ArrayList<>();
    private ArrayList<ProductData> copyItemData = new ArrayList<>();
    private RecyclerView personRecyclerView;
    FirebaseFirestore firestore;
    HomePageRecyclerView adapter;
    private SearchView searchView;
    private ImageButton filterBtn;
    private Spinner dropdownMenu;

    public static PersonFragment newInstance() {
        return new PersonFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.person_fragment, container, false);

        firestore = FirebaseFirestore.getInstance();

        personRecyclerView = view.findViewById(R.id.person_recyclerview);
        searchView = view.findViewById(R.id.person_screen_searchView);
        filterBtn = view.findViewById(R.id.person_screen_filter);
        dropdownMenu = view.findViewById(R.id.person_screen_spinner);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.search(s, copyItemData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!s.isEmpty()){
                    adapter.search(s, copyItemData);
                    return true;
                }
                return true;
            }
        });

        filterBtn.setOnClickListener(view1 -> {
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
         setPersonsInfo();
        return view;
    }

    private void setAdapter() {
        adapter = new HomePageRecyclerView(productItemData);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        personRecyclerView.setLayoutManager(layoutManager);
        personRecyclerView.setItemAnimator(new DefaultItemAnimator());
        personRecyclerView.setAdapter(adapter);
    }

    private void setPersonsInfo(){
        firestore.collection("person").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            String id = d.getId().toString();
                            String name = Objects.requireNonNull(d.getData().get("name")).toString();
                            String price = Objects.requireNonNull(d.getData().get("Price")).toString();
                            float pricefloat = Float.parseFloat(price);
                            String imageUrl = Objects.requireNonNull(d.getData().get("imageurl")).toString();
                            int numOfUnit = Integer.parseInt(d.getData().get("personId").toString());
                            String type = "person";
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        // TODO: Use the ViewModel
    }

}