package com.akhil.microservices.api.core.expense;

public class Category {

    private String name;
    private boolean isFavourite;

    public Category() {
    }

    public Category(String name, boolean isFavourite) {
        this.name = name;
        this.isFavourite = isFavourite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
