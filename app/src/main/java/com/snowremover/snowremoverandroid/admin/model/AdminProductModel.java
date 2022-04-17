package com.snowremover.snowremoverandroid.admin.model;

public class AdminProductModel {
    String id;
    String type;
    String quantity;
    String name;
    String imageUrl;
    public boolean archive;

    public AdminProductModel(String id, String type, String quantity, String name, String imageUrl, boolean archive) {
        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.name = name;
        this.imageUrl = imageUrl;
        this.archive = archive;
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

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}
