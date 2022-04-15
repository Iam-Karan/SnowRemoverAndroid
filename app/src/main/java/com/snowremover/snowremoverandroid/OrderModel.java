package com.snowremover.snowremoverandroid;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderModel implements Serializable {

    public String id;
    public String date;
    public String itemCount;
    public String total;
    public String ImageUrl;
    public boolean isDeliver;
    public ArrayList<OrdrItemModel> prodocts;

    public OrderModel(String id, String date, String itemCount, String total, String imageUrl, boolean isDeliver, ArrayList<OrdrItemModel> prodocts) {
        this.id = id;
        this.date = date;
        this.itemCount = itemCount;
        this.total = total;
        ImageUrl = imageUrl;
        this.isDeliver = isDeliver;
        this.prodocts = prodocts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public boolean isDeliver() {
        return isDeliver;
    }

    public void setDeliver(boolean deliver) {
        isDeliver = deliver;
    }

    public ArrayList<OrdrItemModel> getProdocts() {
        return prodocts;
    }

    public void setProdocts(ArrayList<OrdrItemModel> prodocts) {
        this.prodocts = prodocts;
    }
}
