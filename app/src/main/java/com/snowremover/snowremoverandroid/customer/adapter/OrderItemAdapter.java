package com.snowremover.snowremoverandroid.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.customer.model.OrdrItemModel;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>{
    private ArrayList<OrdrItemModel> cartProductData;
    private Context context;

    public OrderItemAdapter(ArrayList<OrdrItemModel> cartProductData, Context context) {
        this.cartProductData = cartProductData;
        this.context = context;
    }

    public class OrderItemViewHolder extends RecyclerView.ViewHolder{

        private ImageView productImage;
        private TextView productName, productPrice, productQunaity;
        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.order_item_image);
            productName = itemView.findViewById(R.id.order_item_name);
            productPrice = itemView.findViewById(R.id.order_item_price);
            productQunaity = itemView.findViewById(R.id.order_item_quntity);
        }
    }

    @NonNull
    @Override
    public OrderItemAdapter.OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_item_card, parent, false);
        return new OrderItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemAdapter.OrderItemViewHolder holder, int position) {
        String itemName = cartProductData.get(position).getName();
        String priceString = String.valueOf(cartProductData.get(position).getPrice());
        holder.productName.setText(itemName);
        holder.productPrice.setText("$"+priceString);

        if(cartProductData.get(position).getType().equals("personimages")){
            holder.productQunaity.setText("Hours: "+cartProductData.get(position).getHour());
        }else {
            holder.productQunaity.setText("Quantity : "+cartProductData.get(position).getQuantity());
        }
        StorageReference storageReference =  FirebaseStorage.getInstance().getReference(cartProductData.get(position).getImageUrl());

        storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(context)
                        .load(uri)
                        .into(holder.productImage));

    }

    @Override
    public int getItemCount() {
        return cartProductData.size();
    }

}
