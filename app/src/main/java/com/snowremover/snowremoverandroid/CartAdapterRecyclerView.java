package com.snowremover.snowremoverandroid;

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
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CartAdapterRecyclerView extends RecyclerView.Adapter<CartAdapterRecyclerView.CartViewHolder>{

    private  ArrayList<CartModel> cartProductData;
    private  ArrayList<CartModel> copyData;
    private Context context;
    FirebaseFirestore firestore;
    String uId;

    public CartAdapterRecyclerView(ArrayList<CartModel> cartProductData, ArrayList<CartModel> copyData) {
        this.cartProductData = cartProductData;
        this.copyData = copyData;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uId = mFirebaseUser.getUid();
    }


    public class CartViewHolder extends RecyclerView.ViewHolder{
        private MaterialCardView card;
        private ImageView image;
        private TextView name, quantity;
        private AppCompatButton remove;
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
    public CartAdapterRecyclerView.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_screen_card, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String itemName = cartProductData.get(position).getName();
        String priceString = cartProductData.get(position).getQuantity();
        holder.name.setText(itemName);

        if(cartProductData.get(position).getType().equals("person")){
            holder.quantity.setText("");
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference(cartProductData.get(position).getImageUrl());

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
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference(cartProductData.get(position).getImageUrl());

            storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Glide.with(context)
                            .load(uri)
                            .into(holder.image));

            holder.card.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
                intent.putExtra("ProductId", cartProductData.get(position).getId());
                view.getContext().startActivity(intent);
            });
        }

        holder.remove.setOnClickListener(view -> {
            copyData.remove(position);
            removeItemCart(cartProductData.get(position).getId(), context);
        });
    }

    @Override
    public int getItemCount() {
        Log.d("check count", "checked");
        return cartProductData.size();
    }

    public void removeItemCart(String prductId, Context context){
        cartProductData.clear();
        cartProductData.addAll(copyData);
        notifyDataSetChanged();
        firestore.collection("users").document(uId).collection("cart").document(prductId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Item removed From Favourite successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.w("error", e.toString()));
    }


}
