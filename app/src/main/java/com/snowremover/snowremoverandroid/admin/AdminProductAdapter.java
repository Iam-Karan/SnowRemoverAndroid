package com.snowremover.snowremoverandroid.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.CartModel;
import com.snowremover.snowremoverandroid.PersonDetailActivity;
import com.snowremover.snowremoverandroid.ProductData;
import com.snowremover.snowremoverandroid.ProductDetailActivity;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.CartViewHolder>{

    private final ArrayList<AdminProductModel> cartProductData;
    private final ArrayList<AdminProductModel> copyData;
    private Context context;
    FirebaseFirestore firestore;
    String uId;

    public AdminProductAdapter(ArrayList<AdminProductModel> cartProductData, ArrayList<AdminProductModel> copyData) {
        this.cartProductData = cartProductData;
        this.copyData = copyData;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uId = mFirebaseUser.getUid();
    }


    public class CartViewHolder extends RecyclerView.ViewHolder{
        private final MaterialCardView card;
        private final ImageView image;
        private final TextView name;
        private final TextView quantity;
        private final AppCompatButton remove;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_product_image);
            name = itemView.findViewById(R.id.cart_product_name);
            quantity = itemView.findViewById(R.id.cart_product_quntity);
            remove = itemView.findViewById(R.id.cart_product_remove);
            card = itemView.findViewById(R.id.cart_card);
            context = itemView.getContext();
        }
    }

    @NonNull
    @Override
    public AdminProductAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_screen_card, parent, false);
        return new AdminProductAdapter.CartViewHolder(itemView);
    }

    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull AdminProductAdapter.CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String itemName = cartProductData.get(position).getName();
        String priceString = cartProductData.get(position).getQuantity();
        holder.name.setText(itemName);

        if(cartProductData.get(position).getType().equals("person")){
            holder.quantity.setText("");
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("personimages/"+cartProductData.get(position).getImageUrl());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.image));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), PersonDetailActivity.class);
                intent.putExtra("PersonId", cartProductData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }else {
            holder.quantity.setText("Quantity : "+priceString);
            Log.d("url", cartProductData.get(position).getImageUrl());
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference("products/"+cartProductData.get(position).getImageUrl());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.image));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
                intent.putExtra("ProductId", cartProductData.get(position).getId());
                view.getContext().startActivity(intent);
            });

            if(cartProductData.get(position).isArchive()){
                holder.remove.setText("Unarchive");
                holder.remove.setBackground(ContextCompat.getDrawable(context, R.drawable.green_round_btn));
            }else {
                holder.remove.setText("Archive");
                holder.remove.setBackground(ContextCompat.getDrawable(context, R.drawable.pink_round_btn));
            }
            holder.remove.setOnClickListener(view -> {
                boolean archive = cartProductData.get(position).isArchive();
                cartProductData.get(position).archive = !archive;
                copyData.get(position).archive = !archive;
                removeItemCart(cartProductData.get(position).getId(), cartProductData.get(position).getType(), archive);
            });
        }

    }

    @Override
    public int getItemCount() {
        return cartProductData.size();
    }

    public void removeItemCart(String prductId, String type, boolean archive){
        firestore.collection(type).document(prductId).update("archive" , !archive);
        cartProductData.clear();
        cartProductData.addAll(copyData);
        notifyDataSetChanged();
    }

    public void search(String text, ArrayList<AdminProductModel> itemsCopy) {
        cartProductData.clear();
        if(text.isEmpty()){
            cartProductData.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(AdminProductModel item: itemsCopy){
                if(item.getName().toLowerCase().contains(text) || item.getType().toLowerCase().contains(text)){
                    cartProductData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}
