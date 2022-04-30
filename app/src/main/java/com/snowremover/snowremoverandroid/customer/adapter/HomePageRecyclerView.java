package com.snowremover.snowremoverandroid.customer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.PersonDetailActivity;
import com.snowremover.snowremoverandroid.ProductDetailActivity;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.customer.model.ProductData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class HomePageRecyclerView extends RecyclerView.Adapter<HomePageRecyclerView.MyViewHolder> {

    private ArrayList<ProductData> productItemData;
    private Context context;

    public HomePageRecyclerView(ArrayList<ProductData> productItemData) {
        this.productItemData = productItemData;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView itemImage;
        private TextView itemName;
        private TextView itemPrice;
        private MaterialCardView card;
        public MyViewHolder(@NonNull View view) {
            super(view);

            card = view.findViewById(R.id.home_card);
            itemImage = view.findViewById(R.id.home_screen_image);
            itemName = view.findViewById(R.id.home_screen_name);
            itemPrice = view.findViewById(R.id.home_screen_price);
            context = view.getContext();
        }
    }

    @NonNull
    @Override
    public HomePageRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_screen_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String itemName = productItemData.get(position).getName();
        String priceString = String.valueOf(productItemData.get(position).getPrice());
        String itemPrice = "$"+priceString;
        holder.itemName.setText(itemName);
        holder.itemPrice.setText(itemPrice);
        if(productItemData.get(position).getType().equals("person")){
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("personimages/"+productItemData.get(position).getImage());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.itemImage));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), PersonDetailActivity.class);
                intent.putExtra("PersonId", productItemData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }else {
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("products/"+productItemData.get(position).getImage());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.itemImage));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
                intent.putExtra("ProductId", productItemData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }

    }

    public void search(String text, ArrayList<ProductData> itemsCopy) {
        productItemData.clear();
        if(text.isEmpty() || text == null){
            productItemData.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(ProductData item: itemsCopy){
                if(item.name.toLowerCase().contains(text) || item.type.toLowerCase().contains(text)){
                    productItemData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void personSearch(String text, ArrayList<ProductData> itemsCopy) {
        productItemData.clear();
        if(text.isEmpty() || text == null){
            productItemData.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(ProductData item: itemsCopy){
                if(item.name.toLowerCase().contains(text)){
                    productItemData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void available(ArrayList<ProductData> itemsCopy){
        productItemData.clear();
        for(ProductData item: itemsCopy){
            if(item.numOfUnit > 0){
                productItemData.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void favorite(ArrayList<ProductData> itemsCopy, ArrayList<String> favId){
        productItemData.clear();
        Log.d("favid", favId.get(0));
        for(int i = 0; i < itemsCopy.size(); i++){
            if(favId.contains(itemsCopy.get(i).getId())){
                productItemData.add(itemsCopy.get(i));
            }
        }
        notifyDataSetChanged();
    }

    public void lowToHigh(ArrayList<ProductData> itemsCopy){
        productItemData.clear();
        Collections.sort(itemsCopy, Comparator.comparing(ProductData::getPrice));
        productItemData.addAll(itemsCopy);
        notifyDataSetChanged();
    }

    public void highToLow(ArrayList<ProductData> itemsCopy){
        productItemData.clear();
        Collections.sort(itemsCopy, Comparator.comparing(ProductData::getPrice).reversed());
        productItemData.addAll(itemsCopy);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productItemData.size();
    }
}
