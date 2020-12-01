package com.LAM.GiftToMe.FCMFirebase;

public class ModelUser {

    private String username, token;

    public ModelUser(String username, String token){
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
