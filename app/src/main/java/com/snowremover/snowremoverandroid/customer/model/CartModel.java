package com.snowremover.snowremoverandroid.customer.model;

public class CartModel {

    String id;
    String type;
    String quantity;
    String name;
    String imageUrl;
    String hour;
    double price;

    public CartModel(String id, String type, String quantity, String name, String imageUrl, String hour, double price) {
        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.name = name;
        this.imageUrl = imageUrl;
        this.hour = hour;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
