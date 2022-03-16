package com.snowremover.snowremoverandroid;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomePageRecyclerView extends RecyclerView.Adapter<HomePageRecyclerView.MyViewHolder> {

    private ArrayList<ProductData> productItemData;

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
        }
    }

    @NonNull
    @Override
    public HomePageRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_screen_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomePageRecyclerView.MyViewHolder holder, int position) {
        String itemName = productItemData.get(position).getName();
        String itemPrice = "$"+productItemData.get(position).getPrice().toString();
        Picasso.get()
                .load(productItemData.get(position).getImage())
                .placeholder(R.drawable.toolone)
                .error(R.drawable.toolone)
                .into(holder.itemImage);
        holder.itemName.setText(itemName);
        holder.itemPrice.setText(itemPrice);

    }

    @Override
    public int getItemCount() {
        return productItemData.size();
    }
}
