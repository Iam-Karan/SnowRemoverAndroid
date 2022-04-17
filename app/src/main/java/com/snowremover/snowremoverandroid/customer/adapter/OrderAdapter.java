package com.snowremover.snowremoverandroid.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.snowremover.snowremoverandroid.OrderDetailsActivity;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.customer.model.OrderModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{
    private ArrayList<OrderModel> orderData;
    private Context context;
    private String userName;

    public OrderAdapter(ArrayList<OrderModel> orderData, Context context, String userName) {
        this.orderData = orderData;
        this.context = context;
        this.userName = userName;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView orderId, orderDate, orderItemNumber, orderPrice;
        private AppCompatButton isOrderd, isNotOrder;
        private ImageView orderImage;
        private RecyclerView orderRecyclerView;
        private MaterialCardView cardView;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_name);
            orderDate = itemView.findViewById(R.id.order_date);
            orderItemNumber = itemView.findViewById(R.id.order_item_count);
            orderPrice = itemView.findViewById(R.id.order_price);
            orderImage = itemView.findViewById(R.id.order_image);
            isOrderd = itemView.findViewById(R.id.order_is_deliver);
            isNotOrder = itemView.findViewById(R.id.order_is_not_deliver);
            orderRecyclerView = itemView.findViewById(R.id.oder_items_recyclerview);
            cardView = itemView.findViewById(R.id.order_card);
        }
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_card, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        holder.orderId.setText("Order #"+orderData.get(position).getId());
        holder.orderDate.setText(orderData.get(position).getDate());
        holder.orderItemNumber.setText("x"+orderData.get(position).getItemCount()+" Items");
        holder.orderPrice.setText("$"+orderData.get(position).getTotal());
        Picasso.get()
                .load(orderData.get(position).getImageUrl())
                .placeholder(R.drawable.toolone)
                .error(R.drawable.toolone)
                .into(holder.orderImage);


        OrderItemAdapter adapter = new OrderItemAdapter(orderData.get(position).getProdocts(), context);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        holder.orderRecyclerView.setLayoutManager(layoutManager);
        holder.orderRecyclerView.setItemAnimator(new DefaultItemAnimator());
        holder.orderRecyclerView.setAdapter(adapter);

        if(orderData.get(position).isDeliver()){
            holder.isOrderd.setVisibility(View.VISIBLE);
            holder.isNotOrder.setVisibility(View.GONE);
        }else {
            holder.isOrderd.setVisibility(View.GONE);
            holder.isNotOrder.setVisibility(View.VISIBLE);
        }

        holder.cardView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), OrderDetailsActivity.class);
            intent.putExtra("orderId", orderData.get(position).getId());
            intent.putExtra("userName", userName);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderData.size();
    }

}
