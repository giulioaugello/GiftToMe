package com.LAM.GiftToMe.FCMFirebase;

import java.util.ArrayList;

public class ModelUser {

    private String username;
    private String[] token;

    public ModelUser(){

    }

//    public ModelUser(String username, String token){
//        this.username = username;
//        this.token.add(token);
//    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String[] getToken() {
        return token;
    }

    public void setToken(String[] token) {
        this.token = token;
    }
}
