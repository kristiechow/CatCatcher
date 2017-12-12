package com.example.kristie.myapplication;

import java.util.ArrayList;

/**
 * Created by jalenwang on 11/11/17.
 */

public class HistoryInformation extends ArrayList<HistoryInformation> {
    private String image;
    private String catName;
    private String catLong;
    private String catLat;
    private String catPetted;


    public HistoryInformation(String image, String catName, String catLong, String catLat, String catPetted) {
        this.image = image;
        this.catName = catName;
        this.catLong = catLong;
        this.catLat = catLat;
        this.catPetted = catPetted;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatLong() {
        return catLong;
    }

    public void setCatLong(String catLong) {
        this.catLong = catLong;
    }

    public String getCatLat() {
        return catLat;
    }

    public void setCatLat(String catLat) {
        this.catLat = catLat;
    }

    public String getCatPetted() {
        return catPetted;
    }

    public void setCatPetted(String catPetted) {
        this.catPetted = catPetted;
    }
}
