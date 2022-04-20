package com.snowremover.snowremoverandroid.admin.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.admin.adapter.AdminProductAdapter;
import com.snowremover.snowremoverandroid.admin.AdminProductDetailActivity;
import com.snowremover.snowremoverandroid.admin.model.AdminProductModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<AdminProductModel> productItemData = new ArrayList<>();
    private ArrayList<AdminProductModel> copyItemData = new ArrayList<>();
    private RecyclerView homeRecyclerView;
    FirebaseFirestore firestore;
    AdminProductAdapter adapter;
    private SearchView searchView;
    private AppCompatButton addtocartBtn;
    private ImageButton filterBtn;
    private Spinner dropdownMenu;

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminHomeFragment newInstance(String param1, String param2) {
        AdminHomeFragment fragment = new AdminHomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        firestore = FirebaseFirestore.getInstance();

        homeRecyclerView = view.findViewById(R.id.home_recyclerview);
        searchView = view.findViewById(R.id.home_screen_searchView);
        addtocartBtn = view.findViewById(R.id.product_add_to_cart);
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

        addtocartBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(view1.getContext(), AdminProductDetailActivity.class);
            intent.putExtra("ProductId", "");
            view1.getContext().startActivity(intent);
        });


        productItemData = new ArrayList<>();
        setProductsInfo();

        return view;
    }

    private void setAdapter() {
        adapter = new AdminProductAdapter(productItemData, copyItemData);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        homeRecyclerView.setLayoutManager(layoutManager);
        homeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        homeRecyclerView.setAdapter(adapter);
    }

    private void setProductsInfo() {
        ProgressDialog progressdialog = new ProgressDialog(getContext());
        progressdialog.show();
        firestore.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            String id = d.getId();
                            String name = Objects.requireNonNull(d.getData().get("name")).toString();
                            String imageUrl = Objects.requireNonNull(d.getData().get("main_image")).toString();
                            String quantity = d.getData().get("stock_unit").toString();
                            String type = "products";
                            boolean archive = (boolean) d.getData().get("archive");
                            AdminProductModel data = new AdminProductModel(id,type, quantity, name, imageUrl, archive);
                            productItemData.add(data);
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
    }
}