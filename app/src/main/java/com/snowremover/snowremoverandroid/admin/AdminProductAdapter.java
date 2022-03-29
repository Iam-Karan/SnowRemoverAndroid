package com.snowremover.snowremoverandroid.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.CartModel;
import com.snowremover.snowremoverandroid.PersonDetailActivity;
import com.snowremover.snowremoverandroid.ProductDetailActivity;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.CartViewHolder>{

    private final ArrayList<CartModel> cartProductData;
    private Context context;
    FirebaseFirestore firestore;
    String uId;

    public AdminProductAdapter(ArrayList<CartModel> cartProductData) {
        this.cartProductData = cartProductData;
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
        }

        holder.remove.setText("Archive");

        holder.remove.setOnClickListener(view -> removeItemCart(cartProductData.get(position).getId(), context));
    }

    @Override
    public int getItemCount() {
        return cartProductData.size();
    }

    public void removeItemCart(String prductId, Context context){

    }

    public void search(String text, ArrayList<CartModel> itemsCopy) {
        cartProductData.clear();
        if(text.isEmpty()){
            cartProductData.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(CartModel item: itemsCopy){
                if(item.getName().toLowerCase().contains(text) || item.getType().toLowerCase().contains(text)){
                    cartProductData.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

}
