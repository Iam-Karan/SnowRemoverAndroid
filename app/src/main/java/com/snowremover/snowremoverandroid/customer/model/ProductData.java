package com.snowremover.snowremoverandroid.customer.model;

public class ProductData {

    String id;
    public String name;
    String image;
    float price;
    public int numOfUnit;
    public String type;

    public ProductData(String id, String name, String image, float price, int numOfUnit, String type) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.numOfUnit = numOfUnit;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getNumOfUnit() {
        return numOfUnit;
    }

    public void setNumOfUnit(int numOfUnit) {
        this.numOfUnit = numOfUnit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
