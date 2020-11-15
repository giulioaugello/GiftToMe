package com.LAM.GiftToMe.UsefulClass;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

public class UsersGift {

    private String tweetId, giftId, name, description, lat, lon, category, address, issuer;


    public UsersGift(String tweetId, String giftId, String name, String description, String lat, String lon, String category, String address, String issuer) {
        this.tweetId = tweetId;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lon = lon;
        this.category = category;
        this.address = address;
        this.issuer = issuer;
        this.giftId = giftId;
    }

    public UsersGift(){

    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
