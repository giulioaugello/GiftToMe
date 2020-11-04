package com.LAM.GiftToMe.UsefulClass;

public class MyGift {

    private String tweetId,giftId,name,description,lat,lon,category,address,issuer;

    public MyGift(String tweetId,String giftId,String name, String description, String lat, String lon, String category, String address,String issuer) {
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

    public MyGift(){

    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getTweetId() {
        return tweetId;
    }

    public String getGiftId() {
        return giftId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public String getIssuer() {
        return issuer;
    }
}
