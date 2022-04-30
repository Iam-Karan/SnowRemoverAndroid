package com.snowremover.snowremoverandroid.ui.person;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.customer.adapter.HomePageRecyclerView;
import com.snowremover.snowremoverandroid.customer.model.ProductData;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonFragment extends Fragment {

    private PersonViewModel mViewModel;
    private ArrayList<ProductData> productItemData = new ArrayList<>();
    private ArrayList<ProductData> copyItemData = new ArrayList<>();
    private ArrayList<String> favouriteItems = new ArrayList<>();
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
                adapter.personSearch(s, copyItemData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s != null && !copyItemData.isEmpty()){
                    adapter.personSearch(s, copyItemData);
                }
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            adapter.personSearch("", copyItemData);
            return false;
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
                if(i == 0){
                    productItemData.clear();
                    productItemData.addAll(copyItemData);
                    setAdapter();
                }
                if(i == 1){
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                    if(mFirebaseUser != null){
                        adapter.favorite(copyItemData, favouriteItems);
                    }else {
                        Toast.makeText(getContext(), "You need to login first", Toast.LENGTH_SHORT).show();
                    }
                }
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
        favouriteItems.clear();
        productItemData.clear();
        copyItemData.clear();

        ProgressDialog progressdialog = new ProgressDialog(getContext());
        progressdialog.show();
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
                            boolean archive = (boolean) d.getData().get("archive");
                            if(!archive){
                                ProductData data = new ProductData(id, name, imageUrl, pricefloat, numOfUnit, type);
                                productItemData.add(data);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    copyItemData.addAll(productItemData);
                    progressdialog.dismiss();
                    setAdapter();
                });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null){
            String uId = mFirebaseUser.getUid();

            firestore.collection("users").document(uId).collection("favorite").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(Objects.requireNonNull(task.getResult()).size() > 0) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        String docId = document.getId();
                                        favouriteItems.add(docId);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Task Fails to get Favourite products", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        // TODO: Use the ViewModel
    }

}