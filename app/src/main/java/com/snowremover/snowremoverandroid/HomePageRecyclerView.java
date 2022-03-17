package com.snowremover.snowremoverandroid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String itemName = productItemData.get(position).getName();
        String priceString = String.valueOf(productItemData.get(position).getPrice());
        String itemPrice = "$"+priceString;
        StorageReference storageReference =  FirebaseStorage.getInstance().getReference("products/"+productItemData.get(position).getImage());

        storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(context)
                .load(uri)
                .into(holder.itemImage));


        holder.itemName.setText(itemName);
        holder.itemPrice.setText(itemPrice);

    }

    public void search(String text, ArrayList<ProductData> itemsCopy) {
        productItemData.clear();
        if(text.isEmpty()){
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

    public void available(ArrayList<ProductData> itemsCopy){
        productItemData.clear();
        for(ProductData item: itemsCopy){
            if(item.numOfUnit > 0){
                productItemData.add(item);
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
