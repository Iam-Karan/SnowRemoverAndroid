package com.snowremover.snowremoverandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CartApapter extends RecyclerView.Adapter<CartApapter.ViewHolder> {

    private ArrayList<ProductData> productItemData;
    private Context context;

    public CartApapter(ArrayList<ProductData> productItemData) {
        this.productItemData = productItemData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private MaterialCardView card;
        private ImageView image;
        private TextView name, quantity;
        private AppCompatButton remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.cart_product_image);
            name = itemView.findViewById(R.id.cart_product_name);
            quantity = itemView.findViewById(R.id.cart_product_quntity);
            remove = itemView.findViewById(R.id.cart_product_remove);
            context = itemView.getContext();
            card = itemView.findViewById(R.id.cart_card);
        }
    }

    @NonNull
    @Override
    public CartApapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_screen_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String itemName = productItemData.get(position).getName();
        String priceString = String.valueOf(productItemData.get(position).getNumOfUnit());
        String itemQuantity = "$"+priceString;
        holder.name.setText(itemName);

        if(productItemData.get(position).getType().equals("person")){

            holder.quantity.setText("");
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("personimages/"+productItemData.get(position).getImage());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.image));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), PersonDetailActivity.class);
                intent.putExtra("PersonId", productItemData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }else {
            holder.quantity.setText("Quantity : "+itemQuantity);
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("products/"+productItemData.get(position).getImage());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.image));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
                intent.putExtra("ProductId", productItemData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return productItemData.size();
    }
}
